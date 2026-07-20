// MOCK — replaced in B-SWAP-1
// mocks/mock-backend/server.js

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 8080;

// Enable CORS with credentials support
app.use(cors({
  origin: 'http://localhost:5173',
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Authorization', 'Content-Type']
}));

// Body parser
app.use(express.json());

// Log middleware (truncating query strings for safety / presigned URLs simulation)
app.use((req, res, next) => {
  const url = req.originalUrl.split('?')[0];
  console.log(`[MOCK BACKEND] ${req.method} ${url}`);
  next();
});

// Helper for standard response wrapping
const apiSuccess = (message, data) => ({
  success: true,
  message,
  data
});

const apiError = (message, data = null) => ({
  success: false,
  message,
  data
});

const pagedResponse = (content, page = 0, size = 10, totalElements = 0) => {
  const totalPages = Math.ceil(totalElements / size);
  return {
    content,
    page: parseInt(page),
    size: parseInt(size),
    totalElements,
    totalPages,
    last: parseInt(page) >= totalPages - 1
  };
};

// ----------------------------------------------------
// IN-MEMORY STATE SEEDING
// ----------------------------------------------------

// Users
let users = [
  { id: 1, email: 'auditor@acme.com', firstName: 'Ananya', lastName: 'Rao', companyName: 'Acme Global Logistics', role: 'ROLE_AUDITOR', isActive: true, isEmailVerified: true },
  { id: 2, email: 'admin@carbontrace.dev', firstName: 'Admin', lastName: 'User', companyName: 'CarbonTrace Corp', role: 'ROLE_ADMIN', isActive: true, isEmailVerified: true },
  { id: 3, email: 'unverified@acme.com', firstName: 'Unverified', lastName: 'User', companyName: 'Acme Test', role: 'ROLE_AUDITOR', isActive: true, isEmailVerified: false }
];

// Current logged in user (session state for mock)
let currentUserSession = null;

// Vendors (12 to ensure pagination works)
let vendors = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1,
  name: `Logistics Vendor ${i + 1}`,
  contactEmail: `ops${i + 1}@logisticsvendor.com`,
  country: ['Singapore', 'Germany', 'USA', 'India', 'China', 'Netherlands'][i % 6],
  vendorType: 'LOGISTICS',
  isActive: true,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
}));

// Shipments (12 seeded, mix of statuses)
let shipments = Array.from({ length: 12 }, (_, i) => {
  const id = i + 1;
  let status = 'NEEDS_REVIEW';
  if (id === 7) status = 'CALCULATED'; // Required calculated shipment id 7
  if (id === 12) status = 'CALCULATED';
  if (id % 4 === 0) status = 'UPLOADED';
  if (id % 5 === 0) status = 'FAILED';

  return {
    id,
    vendorId: (i % 3) + 1,
    uploadedBy: 1,
    invoiceNumber: `INV-2026-00${id}`,
    carrierName: `Carrier ${['OceanBridge', 'EcoFreight', 'SwiftCargo'][i % 3]}`,
    shipmentDate: `2026-07-${10 + i}`,
    originCity: ['Shenzhen', 'Mumbai', 'New York'][i % 3],
    originCountry: ['China', 'India', 'USA'][i % 3],
    originLat: [22.5431, 19.0760, 40.7128][i % 3],
    originLng: [114.0579, 72.8777, -74.0060][i % 3],
    destinationCity: ['Rotterdam', 'Hamburg', 'Rotterdam'][i % 3],
    destinationCountry: ['Netherlands', 'Germany', 'Netherlands'][i % 3],
    destinationLat: [51.9244, 53.5511, 51.9244][i % 3],
    destinationLng: [4.4777, 9.9937, 4.4777][i % 3],
    transportMode: ['SEA', 'ROAD', 'AIR', 'RAIL'][i % 4],
    fuelType: ['HEAVY_FUEL_OIL', 'DIESEL', 'JET_FUEL', 'ELECTRIC'][i % 4],
    weightTonnes: 10 + i * 2.5,
    distanceKm: status === 'CALCULATED' ? 1000 + i * 200 : null,
    distanceSource: status === 'CALCULATED' ? 'COMPUTED' : null,
    extractionConfidence: ['HIGH', 'MEDIUM', 'LOW'][i % 3],
    totalEmissionsKgco2e: status === 'CALCULATED' ? (10 + i * 2.5) * (1000 + i * 200) * 0.011 : null,
    offsetTonnes: status === 'CALCULATED' ? (id === 12 ? 5.0 : 0) : 0,
    status,
    failureReason: status === 'FAILED' ? 'Document has no extractable text — OCR is not supported in the MVP; enter fields manually' : null,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
});

// Document map to simulate S3 references
let shipmentDocuments = shipments.map((s, i) => ({
  id: i + 1,
  shipmentId: s.id,
  s3Key: `invoices/2026/07/uuid-invoice-${s.id}.pdf`,
  fileName: `invoice-${s.id}.pdf`,
  contentType: 'application/pdf',
  fileSizeBytes: 120000 + i * 1500,
  createdAt: new Date().toISOString()
}));

// Emission Factors (12 factors seeded)
let emissionFactors = [
  { id: 1, region: 'GLOBAL', transportMode: 'ROAD', fuelType: 'ANY', factorKgco2ePerTonneKm: 0.105000, circuityFactor: 1.30, source: 'GLEC Framework (demo)', isActive: true },
  { id: 2, region: 'GLOBAL', transportMode: 'RAIL', fuelType: 'ANY', factorKgco2ePerTonneKm: 0.028000, circuityFactor: 1.20, source: 'GLEC Framework (demo)', isActive: true },
  { id: 3, region: 'GLOBAL', transportMode: 'SEA', fuelType: 'ANY', factorKgco2ePerTonneKm: 0.011000, circuityFactor: 1.15, source: 'GLEC Framework (demo)', isActive: true },
  { id: 4, region: 'GLOBAL', transportMode: 'AIR', fuelType: 'ANY', factorKgco2ePerTonneKm: 0.850000, circuityFactor: 1.05, source: 'GLEC Framework (demo)', isActive: true },
  { id: 5, region: 'India', transportMode: 'ROAD', fuelType: 'DIESEL', factorKgco2ePerTonneKm: 0.115000, circuityFactor: 1.30, source: 'Regional GLEC (demo)', isActive: true },
  { id: 6, region: 'China', transportMode: 'SEA', fuelType: 'HEAVY_FUEL_OIL', factorKgco2ePerTonneKm: 0.012000, circuityFactor: 1.15, source: 'Regional GLEC (demo)', isActive: true },
  ...Array.from({ length: 6 }, (_, i) => ({
    id: i + 7,
    region: ['USA', 'Germany', 'France'][i % 3],
    transportMode: ['ROAD', 'RAIL'][i % 2],
    fuelType: 'ANY',
    factorKgco2ePerTonneKm: 0.095 - i * 0.01,
    circuityFactor: 1.25,
    source: 'GLEC Reference (demo)',
    isActive: true
  }))
];

// Sellers (3 sellers)
let sellers = [
  { id: 1, name: 'EcoOffset Solutions', country: 'Germany', verificationStandard: 'GOLD_STANDARD', rating: 4.8, isActive: true, createdAt: new Date().toISOString() },
  { id: 2, name: 'Verdant Carbon Co.', country: 'Brazil', verificationStandard: 'GOLD_STANDARD', rating: 4.6, isActive: true, createdAt: new Date().toISOString() },
  { id: 3, name: 'Blue Sky Forestry', country: 'Canada', verificationStandard: 'VCS', rating: 4.2, isActive: true, createdAt: new Date().toISOString() }
];

// Carbon Credit Listings (8 listings)
let listings = [
  {
    id: 1,
    sellerId: 1,
    projectName: 'Wind Power Development in Rajasthan',
    projectType: 'RENEWABLE_ENERGY',
    pricePerTonneUsd: 12.00,
    availableTonnes: 1500.000,
    vintageYear: 2024,
    description: 'Avoids fossil fuel power generation across India.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 2,
    sellerId: 2,
    projectName: 'Amazon Basin Reforestation Phase I',
    projectType: 'REFORESTATION',
    pricePerTonneUsd: 13.75,
    availableTonnes: 850.000,
    vintageYear: 2024,
    description: 'Cheaper VCS/Gold standard listing for testing tie-break.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 3,
    sellerId: 2,
    projectName: 'Amazon Basin Reforestation Phase II', // Pinned in Section 8.6
    projectType: 'REFORESTATION',
    pricePerTonneUsd: 14.50,
    availableTonnes: 5200.000,
    vintageYear: 2025,
    description: 'Native species reforestation across 3,000 ha. Verbatim Section 8.6 listing.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 4,
    sellerId: 3,
    projectName: 'British Columbia Forestry Carbon Project',
    projectType: 'REFORESTATION',
    pricePerTonneUsd: 22.50,
    availableTonnes: 2500.000,
    vintageYear: 2023,
    description: 'High price reforestation credit for budget tests.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 5,
    sellerId: 2,
    projectName: 'Methane Capture from Solid Waste Landfills',
    projectType: 'METHANE_CAPTURE',
    pricePerTonneUsd: 9.50,
    availableTonnes: 4000.000,
    vintageYear: 2023,
    description: 'Landfill gas extraction and flaring.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 6,
    sellerId: 3,
    projectName: 'Soil Organic Carbon Enhancement',
    projectType: 'SOIL_CARBON',
    pricePerTonneUsd: 18.00,
    availableTonnes: 0.500, // Small available tonnes for testing low-tonnes error
    vintageYear: 2024,
    description: 'Agricultural soil carbon enhancement.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 7,
    sellerId: 1,
    projectName: 'Solar Clean Cooking Systems in Ghana',
    projectType: 'RENEWABLE_ENERGY',
    pricePerTonneUsd: 15.00,
    availableTonnes: 5.000, // VCS cheaper but outranked by VCS higher standards
    vintageYear: 2024,
    description: 'Distributed clean energy cookstoves.',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 8,
    sellerId: 2,
    projectName: 'Reforestation Inactive Project (Test)',
    projectType: 'REFORESTATION',
    pricePerTonneUsd: 10.00,
    availableTonnes: 1000.000,
    vintageYear: 2022,
    description: 'Should not be visible or purchasable because isActive=false.',
    isActive: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
];

// Offset Purchases (12 seeded offset purchases)
let purchases = Array.from({ length: 12 }, (_, i) => {
  const id = i + 1;
  const listing = listings[i % 7];
  const seller = sellers[(listing.sellerId - 1) % 3];
  return {
    id,
    shipmentId: (i % 12) + 1,
    listingId: listing.id,
    purchasedBy: 1,
    sellerName: seller.name,
    projectName: listing.projectName,
    tonnesPurchased: 2.500 + i * 0.5,
    pricePerTonneUsd: listing.pricePerTonneUsd,
    totalCostUsd: (2.500 + i * 0.5) * listing.pricePerTonneUsd,
    transactionReference: `SIM-9F3A2C${id.toString(16).toUpperCase().padStart(2, '0')}`,
    agentReasoning: 'Selected VCS/Gold Standard project due to optimal balance of high verification standards, ratings, and low pricing.',
    status: 'COMPLETED',
    purchasedAt: new Date(Date.now() - i * 24 * 60 * 60 * 1000).toISOString()
  };
});

// Reduction Goals
let goals = [
  { id: 1, title: 'Cut logistics emissions 30% by 2030', targetYear: 2030, baselineEmissionsKgco2e: 120000.0, targetEmissionsKgco2e: 84000.0, notes: 'Baseline = FY2025 total', createdBy: 1, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
  { id: 2, title: 'Reach net zero for Sea shipping by 2028', targetYear: 2028, baselineEmissionsKgco2e: 50000.0, targetEmissionsKgco2e: 0.0, notes: 'Includes offsetting residual emissions', createdBy: 1, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
  { id: 3, title: 'Future Target Goal (Invalidated target)', targetYear: 2026, baselineEmissionsKgco2e: 10000.0, targetEmissionsKgco2e: 9500.0, notes: 'Mini target', createdBy: 1, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }
];

// ----------------------------------------------------
// AUTHENTICATION INTERCEPTOR / MIDDLEWARE
// ----------------------------------------------------

const authenticateToken = (req, res, next) => {
  // Public paths bypass authentication
  const publicPaths = [
    '/api/auth/register',
    '/api/auth/verify-otp',
    '/api/auth/resend-otp',
    '/api/auth/login',
    '/api/auth/refresh',
    '/api/auth/forgot-password',
    '/api/auth/reset-password',
    '/api/marketplace' // GET marketplace is public per Section 10 security chain
  ];

  // S3 PUT mock route is public
  if (req.method === 'PUT' && req.path.startsWith('/mock-s3/')) {
    return next();
  }

  // Check if matching public GET paths
  const isPublicGet = req.method === 'GET' && publicPaths.some(path => req.path.startsWith(path));
  const isPublicPath = publicPaths.some(path => req.path === path);

  if (isPublicPath || isPublicGet) {
    return next();
  }

  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    return res.status(401).json(apiError('Unauthorized: Missing Authorization header'));
  }

  const token = authHeader.split(' ')[1];
  if (!token) {
    return res.status(401).json(apiError('Unauthorized: Missing Token prefix'));
  }

  if (token === 'expired-token') {
    return res.status(401).json(apiError('Unauthorized: Token has expired'));
  }

  // Extract user if matching standard tokens
  if (token.startsWith('jwt-admin-token')) {
    currentUserSession = users.find(u => u.role === 'ROLE_ADMIN');
  } else {
    currentUserSession = users.find(u => u.role === 'ROLE_AUDITOR');
  }

  next();
};

const requireAdmin = (req, res, next) => {
  if (!currentUserSession || currentUserSession.role !== 'ROLE_ADMIN') {
    return res.status(403).json(apiError('Access Denied: Requires Administrator role'));
  }
  next();
};

app.use(authenticateToken);

// ----------------------------------------------------
// ROUTE IMPLEMENTATIONS
// ----------------------------------------------------

// 1. Auth Module

app.post('/api/auth/register', (req, res) => {
  const { email, firstName, lastName, companyName, role } = req.body;
  if (!email || !firstName || !lastName || !companyName) {
    return res.status(400).json(apiError('Invalid register parameters'));
  }
  // Check duplicate
  if (users.find(u => u.email === email)) {
    return res.status(400).json(apiError('User already exists'));
  }
  // Add unverified user
  const newUser = {
    id: users.length + 1,
    email,
    firstName,
    lastName,
    companyName,
    role: role || 'ROLE_AUDITOR',
    isActive: true,
    isEmailVerified: false
  };
  users.push(newUser);

  res.status(201).json(apiSuccess('Registration successful. An OTP has been sent to your email.', {
    email,
    otpExpiresInMinutes: 10
  }));
});

app.post('/api/auth/verify-otp', (req, res) => {
  const { email, code, purpose } = req.body;

  if (code !== '482913') {
    return res.status(400).json(apiError('Invalid OTP code or expired'));
  }

  const user = users.find(u => u.email === email);
  if (user) {
    user.isEmailVerified = true;
  }

  const isRoleAdmin = user && user.role === 'ROLE_ADMIN';
  const roleName = isRoleAdmin ? 'ROLE_ADMIN' : 'ROLE_AUDITOR';
  const tokenPrefix = isRoleAdmin ? 'jwt-admin-token-' : 'jwt-auditor-token-';

  res.status(200).json(apiSuccess('Email verified successfully', {
    accessToken: `${tokenPrefix}${Date.now()}`,
    refreshToken: `refresh-token-rotated-${Date.now()}`,
    tokenType: 'Bearer',
    userId: user ? user.id : 1,
    email: email,
    role: roleName,
    firstName: user ? user.firstName : 'Ananya',
    lastName: user ? user.lastName : 'Rao'
  }));
});

app.post('/api/auth/resend-otp', (req, res) => {
  res.status(200).json(apiSuccess('OTP resent successfully', { email: req.body.email }));
});

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (email === 'unverified@acme.com') {
    return res.status(400).json(apiError('Email not verified'));
  }

  const user = users.find(u => u.email === email);
  if (!user && email !== 'auditor@acme.com' && email !== 'admin@carbontrace.dev') {
    return res.status(401).json(apiError('Invalid email or password'));
  }

  const isRoleAdmin = email === 'admin@carbontrace.dev' || (user && user.role === 'ROLE_ADMIN');
  const roleName = isRoleAdmin ? 'ROLE_ADMIN' : 'ROLE_AUDITOR';
  const tokenPrefix = isRoleAdmin ? 'jwt-admin-token-' : 'jwt-auditor-token-';

  res.status(200).json(apiSuccess('Login successful', {
    accessToken: `${tokenPrefix}${Date.now()}`,
    refreshToken: `refresh-token-rotated-${Date.now()}`,
    tokenType: 'Bearer',
    userId: user ? user.id : (isRoleAdmin ? 2 : 1),
    email: email,
    role: roleName,
    firstName: user ? user.firstName : (isRoleAdmin ? 'Admin' : 'Ananya'),
    lastName: user ? user.lastName : (isRoleAdmin ? 'User' : 'Rao')
  }));
});

app.post('/api/auth/refresh', (req, res) => {
  res.status(200).json(apiSuccess('Tokens refreshed', {
    accessToken: `jwt-auditor-token-${Date.now()}`,
    refreshToken: `refresh-token-rotated-${Date.now()}`
  }));
});

app.post('/api/auth/forgot-password', (req, res) => {
  // Return success even if email doesn't exist
  res.status(200).json(apiSuccess('If email is registered, password reset OTP is sent.'));
});

app.post('/api/auth/reset-password', (req, res) => {
  res.status(200).json(apiSuccess('Password updated successfully'));
});

// 2. Users Module
app.get('/api/users/me', (req, res) => {
  const user = currentUserSession || users[0];
  res.status(200).json(apiSuccess('Current user profile retrieved', {
    id: user.id,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    companyName: user.companyName,
    role: user.role,
    isActive: user.isActive
  }));
});

app.put('/api/users/me', (req, res) => {
  const { firstName, lastName, companyName } = req.body;
  const user = currentUserSession || users[0];

  if (firstName) user.firstName = firstName;
  if (lastName) user.lastName = lastName;
  if (companyName) user.companyName = companyName;

  res.status(200).json(apiSuccess('Profile updated successfully', {
    id: user.id,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    companyName: user.companyName,
    role: user.role,
    isActive: user.isActive
  }));
});

// 3. Vendors Module
app.post('/api/vendors', (req, res) => {
  const { name, contactEmail, country } = req.body;
  if (!name || !country) {
    return res.status(400).json(apiError('Name and country are required'));
  }
  const newVendor = {
    id: vendors.length + 1,
    name,
    contactEmail,
    country,
    vendorType: 'LOGISTICS',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  vendors.push(newVendor);
  res.status(201).json(apiSuccess('Vendor created successfully', newVendor));
});

app.get('/api/vendors', (req, res) => {
  const { page = 0, size = 10, search = '' } = req.query;
  let filtered = vendors;

  if (search) {
    filtered = vendors.filter(v => v.name.toLowerCase().includes(search.toLowerCase()));
  }

  const startIdx = page * size;
  const sliced = filtered.slice(startIdx, startIdx + parseInt(size));

  res.status(200).json(apiSuccess('Vendors list retrieved', pagedResponse(sliced, page, size, filtered.length)));
});

app.get('/api/vendors/:id', (req, res) => {
  const vendor = vendors.find(v => v.id === parseInt(req.params.id));
  if (!vendor) {
    return res.status(404).json(apiError('Vendor not found'));
  }
  res.status(200).json(apiSuccess('Vendor retrieved', vendor));
});

app.put('/api/vendors/:id', requireAdmin, (req, res) => {
  const vendor = vendors.find(v => v.id === parseInt(req.params.id));
  if (!vendor) {
    return res.status(404).json(apiError('Vendor not found'));
  }
  const { name, contactEmail, country } = req.body;
  if (name) vendor.name = name;
  if (contactEmail) vendor.contactEmail = contactEmail;
  if (country) vendor.country = country;
  vendor.updatedAt = new Date().toISOString();
  res.status(200).json(apiSuccess('Vendor updated successfully', vendor));
});

app.put('/api/vendors/:id/toggle-active', requireAdmin, (req, res) => {
  const vendor = vendors.find(v => v.id === parseInt(req.params.id));
  if (!vendor) {
    return res.status(404).json(apiError('Vendor not found'));
  }
  vendor.isActive = !vendor.isActive;
  vendor.updatedAt = new Date().toISOString();
  res.status(200).json(apiSuccess(`Vendor status toggled to ${vendor.isActive ? 'ACTIVE' : 'INACTIVE'}`, vendor));
});

// 4. Shipment & Upload Module
app.post('/api/shipments/upload-url', (req, res) => {
  const { fileName, contentType } = req.body;
  if (contentType !== 'application/pdf') {
    return res.status(400).json(apiError('Only application/pdf is accepted'));
  }
  const uuid = Math.floor(Math.random() * 1000000).toString(16);
  const sanitizedName = fileName.replace(/[^a-zA-Z0-9._-]/g, '_');
  const s3Key = `invoices/2026/07/${uuid}-${sanitizedName}`;

  res.status(200).json(apiSuccess('Upload URL generated', {
    uploadUrl: `http://localhost:${PORT}/mock-s3/${s3Key}`,
    s3Key,
    expiresInSeconds: 600
  }));
});

// Direct PUT endpoint standing in for direct-to-S3 uploads
app.put('/mock-s3/:key(*)', (req, res) => {
  console.log(`[MOCK S3] Direct PUT upload triggered for key: ${req.params.key}`);
  res.status(200).send('OK');
});

app.post('/api/shipments', (req, res) => {
  const { vendorId, s3Key, fileName, fileSizeBytes } = req.body;
  const vendor = vendors.find(v => v.id === parseInt(vendorId));
  if (!vendor) {
    return res.status(404).json(apiError('Vendor not found'));
  }
  if (!vendor.isActive) {
    return res.status(400).json(apiError('Cannot book shipments against inactive vendor'));
  }

  const shipmentId = shipments.length + 1;
  const newShipment = {
    id: shipmentId,
    vendorId: parseInt(vendorId),
    uploadedBy: currentUserSession ? currentUserSession.id : 1,
    invoiceNumber: `MOCK-INV-${shipmentId}`,
    carrierName: vendor.name,
    shipmentDate: new Date().toISOString().split('T')[0],
    originCity: 'Shenzhen',
    originCountry: 'China',
    originLat: 22.5431,
    originLng: 114.0579,
    destinationCity: 'Rotterdam',
    destinationCountry: 'Netherlands',
    destinationLat: 51.9244,
    destinationLng: 4.4777,
    transportMode: 'SEA',
    fuelType: 'HEAVY_FUEL_OIL',
    weightTonnes: 18.500,
    distanceKm: null,
    distanceSource: null,
    extractionConfidence: 'MEDIUM',
    totalEmissionsKgco2e: null,
    offsetTonnes: 0.000,
    status: 'NEEDS_REVIEW',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const newDoc = {
    id: shipmentDocuments.length + 1,
    shipmentId,
    s3Key,
    fileName,
    contentType: 'application/pdf',
    fileSizeBytes: fileSizeBytes || 102400,
    createdAt: new Date().toISOString()
  };

  shipments.push(newShipment);
  shipmentDocuments.push(newDoc);

  res.status(201).json(apiSuccess('Shipment created and AI fields extracted', {
    ...newShipment,
    fieldConfidence: {
      weightTonnes: 'HIGH',
      transportMode: 'HIGH',
      originCity: 'HIGH',
      destinationCity: 'HIGH',
      fuelType: 'MEDIUM',
      shipmentDate: 'HIGH',
      originLat: 'LOW',
      originLng: 'LOW',
      destinationLat: 'LOW',
      destinationLng: 'LOW'
    }
  }));
});

app.get('/api/shipments', (req, res) => {
  const { page = 0, size = 10, status, vendorId } = req.query;
  let filtered = shipments;

  if (status) {
    filtered = filtered.filter(s => s.status === status);
  }
  if (vendorId) {
    filtered = filtered.filter(s => s.vendorId === parseInt(vendorId));
  }

  // Sort by id desc (newest first)
  filtered = [...filtered].sort((a, b) => b.id - a.id);

  const startIdx = page * size;
  const sliced = filtered.slice(startIdx, startIdx + parseInt(size));

  res.status(200).json(apiSuccess('Shipment list retrieved', pagedResponse(sliced, page, size, filtered.length)));
});

app.get('/api/shipments/map', (req, res) => {
  // Only CALCULATED shipments with coordinates
  const mapped = shipments
    .filter(s => s.status === 'CALCULATED' && s.originLat && s.destinationLat)
    .map(s => ({
      id: s.id,
      originLat: s.originLat,
      originLng: s.originLng,
      destinationLat: s.destinationLat,
      destinationLng: s.destinationLng,
      originCity: s.originCity,
      destinationCity: s.destinationCity,
      transportMode: s.transportMode,
      totalEmissionsKgco2e: s.totalEmissionsKgco2e,
      status: s.status
    }));

  res.status(200).json(apiSuccess('Emissions map markers retrieved', mapped));
});

app.get('/api/shipments/:id', (req, res) => {
  const shipment = shipments.find(s => s.id === parseInt(req.params.id));
  if (!shipment) {
    return res.status(404).json(apiError('Shipment not found'));
  }
  res.status(200).json(apiSuccess('Shipment retrieved', shipment));
});

app.get('/api/shipments/:id/document-url', (req, res) => {
  const doc = shipmentDocuments.find(d => d.shipmentId === parseInt(req.params.id));
  if (!doc) {
    return res.status(404).json(apiError('Shipment document not found'));
  }
  res.status(200).json(apiSuccess('Document presigned URL retrieved', {
    documentUrl: `http://localhost:${PORT}/mock-s3/${doc.s3Key}`,
    expiresInSeconds: 900
  }));
});

app.put('/api/shipments/:id/review', (req, res) => {
  const shipment = shipments.find(s => s.id === parseInt(req.params.id));
  if (!shipment) {
    return res.status(404).json(apiError('Shipment not found'));
  }

  // Reject review on pre-seeded calculated shipment id 7 (CALCULATED status is terminal for review)
  if (shipment.id === 7 || shipment.status === 'CALCULATED') {
    return res.status(400).json(apiError('already calculated'));
  }

  // Update fields
  const fields = [
    'invoiceNumber', 'carrierName', 'shipmentDate',
    'originCity', 'originCountry', 'originLat', 'originLng',
    'destinationCity', 'destinationCountry', 'destinationLat', 'destinationLng',
    'transportMode', 'fuelType', 'weightTonnes', 'distanceKm'
  ];

  fields.forEach(field => {
    if (req.body[field] !== undefined) {
      shipment[field] = req.body[field];
    }
  });

  shipment.status = 'REVIEWED';
  shipment.updatedAt = new Date().toISOString();

  res.status(200).json(apiSuccess('Shipment extraction updated/reviewed successfully', shipment));
});

app.post('/api/shipments/:id/calculate', (req, res) => {
  // Handle mock AI service unavailable error case for 502 testing
  if (parseInt(req.params.id) === 502) {
    return res.status(502).json(apiError('AI service unavailable — please try again'));
  }

  const shipment = shipments.find(s => s.id === parseInt(req.params.id));
  if (!shipment) {
    return res.status(404).json(apiError('Shipment not found'));
  }

  // Standard calculation logic
  const weight = shipment.weightTonnes || 18.500;
  const factor = 0.011; // Sea fuel factor
  const circuity = 1.15;

  let distance = shipment.distanceKm;
  if (!distance) {
    // Great circle calculation approximation
    distance = 20430.00;
    shipment.distanceSource = 'COMPUTED';
  } else {
    shipment.distanceSource = 'DOCUMENT';
  }

  shipment.distanceKm = distance;
  shipment.totalEmissionsKgco2e = parseFloat((weight * distance * factor * circuity).toFixed(3));
  shipment.status = 'CALCULATED';
  shipment.updatedAt = new Date().toISOString();

  res.status(200).json(apiSuccess('Emissions calculated', shipment));
});

// 5. Emission Factors Module
app.get('/api/emission-factors', (req, res) => {
  const { page = 0, size = 10 } = req.query;
  const startIdx = page * size;
  const sliced = emissionFactors.slice(startIdx, startIdx + parseInt(size));
  res.status(200).json(apiSuccess('Emission factors retrieved', pagedResponse(sliced, page, size, emissionFactors.length)));
});

app.post('/api/emission-factors', requireAdmin, (req, res) => {
  const { region, transportMode, fuelType, factorKgco2ePerTonneKm, circuityFactor, source } = req.body;
  if (!region || !transportMode || !fuelType || factorKgco2ePerTonneKm === undefined) {
    return res.status(400).json(apiError('Missing required emission factor fields'));
  }
  const newFactor = {
    id: emissionFactors.length + 1,
    region,
    transportMode,
    fuelType,
    factorKgco2ePerTonneKm: parseFloat(factorKgco2ePerTonneKm),
    circuityFactor: parseFloat(circuityFactor) || 1.20,
    source: source || 'Admin seed',
    isActive: true
  };
  emissionFactors.push(newFactor);
  res.status(201).json(apiSuccess('Emission factor created', newFactor));
});

app.put('/api/emission-factors/:id', requireAdmin, (req, res) => {
  const factor = emissionFactors.find(f => f.id === parseInt(req.params.id));
  if (!factor) {
    return res.status(404).json(apiError('Emission factor not found'));
  }
  const fields = ['region', 'transportMode', 'fuelType', 'factorKgco2ePerTonneKm', 'circuityFactor', 'source'];
  fields.forEach(field => {
    if (req.body[field] !== undefined) {
      factor[field] = req.body[field];
    }
  });
  res.status(200).json(apiSuccess('Emission factor updated', factor));
});

app.put('/api/emission-factors/:id/toggle-active', requireAdmin, (req, res) => {
  const factor = emissionFactors.find(f => f.id === parseInt(req.params.id));
  if (!factor) {
    return res.status(404).json(apiError('Emission factor not found'));
  }
  factor.isActive = !factor.isActive;
  res.status(200).json(apiSuccess(`Emission factor state set to ${factor.isActive ? 'ACTIVE' : 'INACTIVE'}`, factor));
});

// 6. Marketplace Module
app.get('/api/marketplace/credits', (req, res) => {
  const { projectType, maxPricePerTonne, minAvailableTonnes, page = 0, size = 20 } = req.query;
  let filtered = listings.filter(l => l.isActive);

  if (projectType) {
    filtered = filtered.filter(l => l.projectType === projectType);
  }
  if (maxPricePerTonne) {
    filtered = filtered.filter(l => l.pricePerTonneUsd <= parseFloat(maxPricePerTonne));
  }
  if (minAvailableTonnes) {
    filtered = filtered.filter(l => l.availableTonnes >= parseFloat(minAvailableTonnes));
  }

  // Populate seller fields dynamically for frontend Section 8.6 requirement
  const content = filtered.map(l => {
    const s = sellers.find(sel => sel.id === l.sellerId) || sellers[0];
    return {
      id: l.id,
      sellerId: l.sellerId,
      sellerName: s.name,
      sellerCountry: s.country,
      sellerRating: s.rating,
      verificationStandard: s.verificationStandard,
      projectName: l.projectName,
      projectType: l.projectType,
      pricePerTonneUsd: l.pricePerTonneUsd,
      availableTonnes: l.availableTonnes,
      vintageYear: l.vintageYear,
      description: l.description
    };
  });

  const startIdx = page * size;
  const sliced = content.slice(startIdx, startIdx + parseInt(size));

  res.status(200).json(apiSuccess('Marketplace listings retrieved', pagedResponse(sliced, page, size, content.length)));
});

app.get('/api/marketplace/credits/:id', (req, res) => {
  const l = listings.find(lst => lst.id === parseInt(req.params.id));
  if (!l) {
    return res.status(404).json(apiError('Listing not found'));
  }
  const s = sellers.find(sel => sel.id === l.sellerId) || sellers[0];
  res.status(200).json(apiSuccess('Marketplace listing details retrieved', {
    id: l.id,
    sellerId: l.sellerId,
    sellerName: s.name,
    sellerCountry: s.country,
    sellerRating: s.rating,
    verificationStandard: s.verificationStandard,
    projectName: l.projectName,
    projectType: l.projectType,
    pricePerTonneUsd: l.pricePerTonneUsd,
    availableTonnes: l.availableTonnes,
    vintageYear: l.vintageYear,
    description: l.description
  }));
});

app.get('/api/marketplace/sellers', (req, res) => {
  const { page = 0, size = 10 } = req.query;
  const startIdx = page * size;
  const sliced = sellers.slice(startIdx, startIdx + parseInt(size));
  res.status(200).json(apiSuccess('Sellers list retrieved', pagedResponse(sliced, page, size, sellers.length)));
});

app.get('/api/marketplace/sellers/:id', (req, res) => {
  const s = sellers.find(sel => sel.id === parseInt(req.params.id));
  if (!s) {
    return res.status(404).json(apiError('Seller not found'));
  }
  res.status(200).json(apiSuccess('Seller retrieved', s));
});

// Admin Marketplace Endpoints
app.post('/api/admin/marketplace/sellers', requireAdmin, (req, res) => {
  const { name, country, verificationStandard, rating } = req.body;
  if (!name || !country || !verificationStandard) {
    return res.status(400).json(apiError('Missing required seller parameters'));
  }
  const newSeller = {
    id: sellers.length + 1,
    name,
    country,
    verificationStandard,
    rating: parseFloat(rating) || 4.0,
    isActive: true,
    createdAt: new Date().toISOString()
  };
  sellers.push(newSeller);
  res.status(201).json(apiSuccess('Seller created', newSeller));
});

app.put('/api/admin/marketplace/sellers/:id', requireAdmin, (req, res) => {
  const s = sellers.find(sel => sel.id === parseInt(req.params.id));
  if (!s) {
    return res.status(404).json(apiError('Seller not found'));
  }
  const fields = ['name', 'country', 'verificationStandard', 'rating', 'isActive'];
  fields.forEach(field => {
    if (req.body[field] !== undefined) {
      s[field] = req.body[field];
    }
  });
  res.status(200).json(apiSuccess('Seller updated', s));
});

app.post('/api/admin/marketplace/credits', requireAdmin, (req, res) => {
  const { sellerId, projectName, projectType, pricePerTonneUsd, availableTonnes, vintageYear, description } = req.body;
  if (!sellerId || !projectName || !projectType || pricePerTonneUsd === undefined || availableTonnes === undefined || !vintageYear) {
    return res.status(400).json(apiError('Missing required listing parameters'));
  }
  const newListing = {
    id: listings.length + 1,
    sellerId: parseInt(sellerId),
    projectName,
    projectType,
    pricePerTonneUsd: parseFloat(pricePerTonneUsd),
    availableTonnes: parseFloat(availableTonnes),
    vintageYear: parseInt(vintageYear),
    description: description || '',
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  listings.push(newListing);
  res.status(201).json(apiSuccess('Marketplace listing created', newListing));
});

app.put('/api/admin/marketplace/credits/:id', requireAdmin, (req, res) => {
  const l = listings.find(lst => lst.id === parseInt(req.params.id));
  if (!l) {
    return res.status(404).json(apiError('Listing not found'));
  }
  const fields = ['pricePerTonneUsd', 'availableTonnes', 'isActive', 'description'];
  fields.forEach(field => {
    if (req.body[field] !== undefined) {
      if (field === 'pricePerTonneUsd' || field === 'availableTonnes') {
        l[field] = parseFloat(req.body[field]);
      } else {
        l[field] = req.body[field];
      }
    }
  });
  l.updatedAt = new Date().toISOString();
  res.status(200).json(apiSuccess('Marketplace listing updated', l));
});

// 7. Purchase Module
app.post('/api/purchases', (req, res) => {
  const { shipmentId, maxBudgetUsd } = req.body;
  const shipment = shipments.find(s => s.id === parseInt(shipmentId));

  if (!shipment) {
    return res.status(404).json(apiError('Shipment not found'));
  }
  if (shipment.status !== 'CALCULATED') {
    return res.status(400).json(apiError('Shipment must be in CALCULATED status to purchase offsets'));
  }

  // Calculate required tonnes = totalEmissionsKgco2e/1000 - offsetTonnes
  const totalKg = shipment.totalEmissionsKgco2e || 4218.750;
  const offset = shipment.offsetTonnes || 0.000;
  const requiredTonnes = Math.max(Math.ceil((totalKg / 1000 - offset) * 1000) / 1000, 0);

  if (requiredTonnes <= 0) {
    return res.status(400).json(apiError('Shipment is already fully offset'));
  }

  // Simulate NO_PURCHASE logic if budget is too low (<= 20)
  if (maxBudgetUsd !== undefined && parseFloat(maxBudgetUsd) <= 20) {
    return res.status(200).json(apiSuccess('NO_PURCHASE decision returned by AI Agent', {
      decision: 'NO_PURCHASE',
      listingId: null,
      tonnes: null,
      pricePerTonneUsd: null,
      totalCostUsd: null,
      transactionReference: null,
      reasoning: `No active listing offers ${requiredTonnes} tonnes within the $${maxBudgetUsd} budget; the cheapest viable option costs $57.98.`,
      status: 'FAILED'
    }));
  }

  // Pick listing 3 (Amazon Basin Reforestation Phase II) verbatim, price 14.50
  const listing = listings.find(l => l.id === 3) || listings[2];
  const seller = sellers.find(s => s.id === listing.sellerId) || sellers[1];

  const cost = requiredTonnes * listing.pricePerTonneUsd;

  // Deduct inventory
  listing.availableTonnes = Math.max(0, listing.availableTonnes - requiredTonnes);
  shipment.offsetTonnes += requiredTonnes;

  const purchaseId = purchases.length + 1;
  const newPurchase = {
    id: purchaseId,
    shipmentId: parseInt(shipmentId),
    listingId: listing.id,
    purchasedBy: currentUserSession ? currentUserSession.id : 1,
    sellerName: seller.name,
    projectName: listing.projectName,
    tonnesPurchased: requiredTonnes,
    pricePerTonneUsd: listing.pricePerTonneUsd,
    totalCostUsd: parseFloat(cost.toFixed(2)),
    transactionReference: `SIM-${Math.random().toString(16).substring(2, 10).toUpperCase()}`,
    agentReasoning: 'Selected Gold Standard reforestation credits at the lowest price per tonne among listings with sufficient availability; total cost is within the stated budget.',
    status: 'COMPLETED',
    purchasedAt: new Date().toISOString()
  };

  purchases.push(newPurchase);

  res.status(201).json(apiSuccess('Offset purchased (simulated)', newPurchase));
});

app.get('/api/purchases', (req, res) => {
  const { shipmentId, page = 0, size = 10 } = req.query;
  let filtered = purchases;

  if (shipmentId) {
    filtered = filtered.filter(p => p.shipmentId === parseInt(shipmentId));
  }

  // Sort by id desc (newest first)
  filtered = [...filtered].sort((a, b) => b.id - a.id);

  const startIdx = page * size;
  const sliced = filtered.slice(startIdx, startIdx + parseInt(size));

  res.status(200).json(apiSuccess('Offset purchases list retrieved', pagedResponse(sliced, page, size, filtered.length)));
});

app.get('/api/purchases/:id', (req, res) => {
  const purchase = purchases.find(p => p.id === parseInt(req.params.id));
  if (!purchase) {
    return res.status(404).json(apiError('Purchase record not found'));
  }
  res.status(200).json(apiSuccess('Purchase record retrieved', purchase));
});

// 8. Goals Module
app.post('/api/goals', (req, res) => {
  const { title, targetYear, baselineEmissionsKgco2e, targetEmissionsKgco2e, notes } = req.body;
  if (!title || !targetYear || !baselineEmissionsKgco2e || !targetEmissionsKgco2e) {
    return res.status(400).json(apiError('Missing required reduction goal fields'));
  }

  if (parseFloat(targetEmissionsKgco2e) >= parseFloat(baselineEmissionsKgco2e)) {
    return res.status(400).json(apiError('Target emissions must be less than baseline emissions'));
  }

  const currentYear = new Date().getFullYear();
  if (parseInt(targetYear) < currentYear) {
    return res.status(400).json(apiError('Target year must be the current year or later'));
  }

  const newGoal = {
    id: goals.length + 1,
    title,
    targetYear: parseInt(targetYear),
    baselineEmissionsKgco2e: parseFloat(baselineEmissionsKgco2e),
    targetEmissionsKgco2e: parseFloat(targetEmissionsKgco2e),
    notes,
    createdBy: currentUserSession ? currentUserSession.id : 1,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  goals.push(newGoal);

  res.status(201).json(apiSuccess('Reduction goal created successfully', newGoal));
});

app.get('/api/goals', (req, res) => {
  res.status(200).json(apiSuccess('Reduction goals list retrieved', goals));
});

app.get('/api/goals/:id', (req, res) => {
  const goal = goals.find(g => g.id === parseInt(req.params.id));
  if (!goal) {
    return res.status(404).json(apiError('Goal not found'));
  }
  res.status(200).json(apiSuccess('Goal retrieved', goal));
});

app.put('/api/goals/:id', (req, res) => {
  const goal = goals.find(g => g.id === parseInt(req.params.id));
  if (!goal) {
    return res.status(404).json(apiError('Goal not found'));
  }
  const { title, targetYear, baselineEmissionsKgco2e, targetEmissionsKgco2e, notes } = req.body;

  if (targetEmissionsKgco2e !== undefined && baselineEmissionsKgco2e !== undefined) {
    if (parseFloat(targetEmissionsKgco2e) >= parseFloat(baselineEmissionsKgco2e)) {
      return res.status(400).json(apiError('Target emissions must be less than baseline emissions'));
    }
  }

  if (targetYear !== undefined) {
    const currentYear = new Date().getFullYear();
    if (parseInt(targetYear) < currentYear) {
      return res.status(400).json(apiError('Target year must be the current year or later'));
    }
  }

  if (title) goal.title = title;
  if (targetYear) goal.targetYear = parseInt(targetYear);
  if (baselineEmissionsKgco2e) goal.baselineEmissionsKgco2e = parseFloat(baselineEmissionsKgco2e);
  if (targetEmissionsKgco2e) goal.targetEmissionsKgco2e = parseFloat(targetEmissionsKgco2e);
  if (notes) goal.notes = notes;
  goal.updatedAt = new Date().toISOString();

  res.status(200).json(apiSuccess('Reduction goal updated successfully', goal));
});

app.delete('/api/goals/:id', (req, res) => {
  const idx = goals.findIndex(g => g.id === parseInt(req.params.id));
  if (idx === -1) {
    return res.status(404).json(apiError('Goal not found'));
  }
  goals.splice(idx, 1);
  res.status(200).json(apiSuccess('Reduction goal deleted successfully'));
});

// 9. Analytics Module
app.get('/api/analytics/dashboard', (req, res) => {
  // Aggregate stats
  const totalShipments = shipments.length;
  const calculatedShipments = shipments.filter(s => s.status === 'CALCULATED').length;
  const totalEmissionsKgco2e = shipments
    .filter(s => s.status === 'CALCULATED')
    .reduce((sum, s) => sum + s.totalEmissionsKgco2e, 0);

  const totalOffsetTonnes = shipments.reduce((sum, s) => sum + (s.offsetTonnes || 0), 0);
  const netEmissionsKgco2e = Math.max(totalEmissionsKgco2e - totalOffsetTonnes * 1000, 0);

  const totalOffsetSpendUsd = purchases
    .filter(p => p.status === 'COMPLETED')
    .reduce((sum, p) => sum + p.totalCostUsd, 0);

  // Mode breakdowns
  const emissionsByMode = { SEA: 0, ROAD: 0, AIR: 0, RAIL: 0 };
  shipments
    .filter(s => s.status === 'CALCULATED')
    .forEach(s => {
      if (emissionsByMode[s.transportMode] !== undefined) {
        emissionsByMode[s.transportMode] += s.totalEmissionsKgco2e;
      }
    });

  // Vendor breakdowns
  const vendorMap = {};
  shipments
    .filter(s => s.status === 'CALCULATED')
    .forEach(s => {
      const v = vendors.find(vend => vend.id === s.vendorId);
      const name = v ? v.name : 'Unknown';
      if (!vendorMap[s.vendorId]) {
        vendorMap[s.vendorId] = { vendorId: s.vendorId, vendorName: name, emissionsKgco2e: 0 };
      }
      vendorMap[s.vendorId].emissionsKgco2e += s.totalEmissionsKgco2e;
    });
  const emissionsByVendor = Object.values(vendorMap);

  // Last 6 months emissions (hardcoded/simulated trend data)
  const monthlyEmissions = {
    '2026-02': 21000.0,
    '2026-03': 33000.5,
    '2026-04': 28000.0,
    '2026-05': 41000.0,
    '2026-06': 39429.75,
    '2026-07': parseFloat(totalEmissionsKgco2e.toFixed(2))
  };

  // Populate goals with computed progressPercent
  // progressPercent = clamp((baseline − currentNetAnnualEmissions) / (baseline − target) × 100, 0, 100)
  const currentNetAnnualEmissions = netEmissionsKgco2e;
  const mappedGoals = goals.map(g => {
    const denom = g.baselineEmissionsKgco2e - g.targetEmissionsKgco2e;
    let progressPercent = 0;
    if (denom !== 0) {
      const numerator = g.baselineEmissionsKgco2e - currentNetAnnualEmissions;
      progressPercent = Math.max(0, Math.min(100, Math.round((numerator / denom) * 100)));
    }
    return {
      ...g,
      progressPercent
    };
  });

  res.status(200).json(apiSuccess('Dashboard analytics aggregates retrieved', {
    totalShipments,
    calculatedShipments,
    totalEmissionsKgco2e,
    totalOffsetTonnes,
    netEmissionsKgco2e,
    totalOffsetSpendUsd,
    emissionsByMode,
    emissionsByVendor,
    monthlyEmissions,
    goals: mappedGoals
  }));
});

// 10. Admin User Management
app.get('/api/admin/users', requireAdmin, (req, res) => {
  const { page = 0, size = 10, role } = req.query;
  let filtered = users;
  if (role) {
    filtered = filtered.filter(u => u.role === role);
  }
  const startIdx = page * size;
  const sliced = filtered.slice(startIdx, startIdx + parseInt(size));
  res.status(200).json(apiSuccess('User account list retrieved (admin)', pagedResponse(sliced, page, size, filtered.length)));
});

app.put('/api/admin/users/:id/toggle-active', requireAdmin, (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id));
  if (!user) {
    return res.status(404).json(apiError('User account not found'));
  }
  // Don't let admin lock themselves
  if (user.email === 'admin@carbontrace.dev') {
    return res.status(400).json(apiError('Cannot deactivate own administrator account'));
  }

  user.isActive = !user.isActive;
  res.status(200).json(apiSuccess(`User account status toggled to ${user.isActive ? 'ACTIVE' : 'INACTIVE'}`, {
    id: user.id,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    companyName: user.companyName,
    role: user.role,
    isActive: user.isActive
  }));
});

// Start Server
app.listen(PORT, () => {
  console.log(`[MOCK BACKEND] CarbonTrace Spring Boot Server listening on port ${PORT}`);
});
