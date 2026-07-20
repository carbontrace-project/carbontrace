import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import apiClient from '../../api/axiosConfig';
import ErrorMessage from '../common/ErrorMessage';
import { formatNumber } from '../../utils/formatters';
import styles from './UploadDropzone.module.css';

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

function UploadDropzone() {
  const [vendors, setVendors] = useState([]);
  const [selectedVendorId, setSelectedVendorId] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragOver, setIsDragOver] = useState(false);

  const [uploadState, setUploadState] = useState('idle'); // idle | requesting_url | uploading_s3 | creating_shipment | success | error
  const [uploadProgress, setUploadProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState(null);

  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  // Load vendors list for select dropdown
  useEffect(() => {
    const fetchVendors = async () => {
      try {
        const response = await apiClient.get('/api/vendors?page=0&size=100');
        const content = response.data?.data?.content || response.data?.content || response.data?.data || [];
        setVendors(content.filter((v) => v.isActive));
        if (content.length > 0) {
          setSelectedVendorId(content[0].id.toString());
        }
      } catch {
        setErrorMessage('Failed to load logistics vendors. Please refresh page.');
      }
    };
    fetchVendors();
  }, []);

  const validateFile = (file) => {
    if (!file) return false;
    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      setErrorMessage('Invalid file format. Only PDF documents (application/pdf) are supported.');
      return false;
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      setErrorMessage(
        `File size (${formatNumber(file.size / (1024 * 1024), 2)} MB) exceeds the 10 MB limit.`
      );
      return false;
    }
    setErrorMessage(null);
    return true;
  };

  const handleFileSelect = (file) => {
    if (validateFile(file)) {
      setSelectedFile(file);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragOver(true);
  };

  const handleDragLeave = () => {
    setIsDragOver(false);
  };

  // Direct S3 Upload Flow Execution
  const executeUploadFlow = async () => {
    if (!selectedVendorId) {
      setErrorMessage('Please select a logistics vendor.');
      return;
    }
    if (!selectedFile) {
      setErrorMessage('Please select a PDF freight invoice file.');
      return;
    }

    setErrorMessage(null);
    setUploadState('requesting_url');
    setUploadProgress(10);

    try {
      // Step 1: Request Presigned Upload URL from Spring Boot
      const urlResponse = await apiClient.post('/api/shipments/upload-url', {
        vendorId: Number(selectedVendorId),
        fileName: selectedFile.name,
        contentType: 'application/pdf',
        fileSizeBytes: selectedFile.size,
      });

      const urlData = urlResponse.data?.data || urlResponse.data;
      const { uploadUrl, s3Key } = urlData;

      if (!uploadUrl || !s3Key) {
        throw new Error('Server failed to return presigned upload URL');
      }

      // Step 2: Bare Axios PUT directly to S3 (NO Authorization header!)
      setUploadState('uploading_s3');
      await axios.put(uploadUrl, selectedFile, {
        headers: {
          'Content-Type': 'application/pdf',
        },
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 80) / progressEvent.total) + 10;
            setUploadProgress(percent);
          }
        },
      });

      // Step 3: Confirm Upload & Trigger Synchronous AI Extraction
      setUploadState('creating_shipment');
      setUploadProgress(95);

      const shipmentResponse = await apiClient.post('/api/shipments', {
        vendorId: Number(selectedVendorId),
        s3Key,
        fileName: selectedFile.name,
        fileSizeBytes: selectedFile.size,
      });

      const shipment = shipmentResponse.data?.data || shipmentResponse.data;
      setUploadState('success');
      setUploadProgress(100);

      // Step 4: Navigate to Shipment Detail Page for Review
      if (shipment?.id) {
        navigate(`/shipments/${shipment.id}`);
      } else {
        navigate('/shipments');
      }
    } catch (err) {
      setUploadState('error');
      const msg = err.response?.data?.message || err.message || 'Direct S3 file upload failed. Please try again.';
      setErrorMessage(msg);
    }
  };

  const isUploading =
    uploadState === 'requesting_url' ||
    uploadState === 'uploading_s3' ||
    uploadState === 'creating_shipment';

  return (
    <div className={styles.container}>
      {errorMessage && <ErrorMessage title="Upload Error" message={errorMessage} />}

      <div className={styles.fieldGroup}>
        <label htmlFor="vendor-select" className={styles.label}>
          Select Logistics Vendor *
        </label>
        <select
          id="vendor-select"
          className={styles.select}
          value={selectedVendorId}
          onChange={(e) => setSelectedVendorId(e.target.value)}
          disabled={isUploading}
        >
          {vendors.length === 0 ? (
            <option value="">No active vendors found</option>
          ) : (
            vendors.map((v) => (
              <option key={v.id} value={v.id}>
                {v.name} ({v.country})
              </option>
            ))
          )}
        </select>
      </div>

      <div
        className={`${styles.dropzone} ${isDragOver ? styles.dropzoneActive : ''} ${
          isUploading ? styles.dropzoneDisabled : ''
        }`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onClick={() => !isUploading && fileInputRef.current?.click()}
      >
        <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="17 8 12 3 7 8" />
          <line x1="12" y1="3" x2="12" y2="15" />
        </svg>

        <h3 className={styles.dropText}>
          {selectedFile ? selectedFile.name : 'Drag & drop freight bill PDF here'}
        </h3>
        <p className={styles.dropSubtext}>
          {selectedFile
            ? `Size: ${formatNumber(selectedFile.size / 1024, 1)} KB (PDF document)`
            : 'Only PDF invoices up to 10 MB are supported.'}
        </p>

        <input
          ref={fileInputRef}
          type="file"
          accept="application/pdf,.pdf"
          style={{ display: 'none' }}
          onChange={(e) => e.target.files?.[0] && handleFileSelect(e.target.files[0])}
          disabled={isUploading}
        />

        <button type="button" className="btn-secondary" disabled={isUploading}>
          Browse Local Files
        </button>
      </div>

      {isUploading && (
        <div className={styles.progressContainer}>
          <span className={styles.progressText}>
            {uploadState === 'requesting_url' && 'Generating presigned S3 URL...'}
            {uploadState === 'uploading_s3' && `Uploading file to S3... ${uploadProgress}%`}
            {uploadState === 'creating_shipment' && 'Extracting invoice data with AI...'}
          </span>
          <div className={styles.progressBarTrack}>
            <div className={styles.progressBarFill} style={{ width: `${uploadProgress}%` }} />
          </div>
        </div>
      )}

      {selectedFile && !isUploading && (
        <button
          type="button"
          className={`btn-primary ${styles.uploadSubmitBtn}`}
          onClick={executeUploadFlow}
        >
          {uploadState === 'error' ? 'Retry Upload to S3' : 'Upload Freight Bill & Analyze'}
        </button>
      )}
    </div>
  );
}

export default UploadDropzone;
