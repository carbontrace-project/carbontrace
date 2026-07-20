import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapContainer, TileLayer, Marker, Polyline, Popup } from 'react-leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import { formatEmissions, formatNumber } from '../../utils/formatters';
import styles from './EmissionsMap.module.css';

// Fix Vite Leaflet default marker icon asset URL resolution
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

function EmissionsMap({ shipments = [] }) {
  // Filter shipments with valid origin & destination coordinates
  const validShipments = (shipments || []).filter(
    (s) =>
      s.originLat !== null &&
      s.originLat !== undefined &&
      s.originLng !== null &&
      s.originLng !== undefined &&
      s.destinationLat !== null &&
      s.destinationLat !== undefined &&
      s.destinationLng !== null &&
      s.destinationLng !== undefined
  );

  const defaultCenter = [20.0, 30.0]; // World view center

  const getPolylineColor = (emissions) => {
    if (!emissions) return 'var(--primary)';
    if (emissions > 5000) return '#d84315'; // High emissions - red/orange
    if (emissions > 2000) return '#f57f17'; // Medium - amber
    return 'var(--primary)'; // Low/Standard - green
  };

  const getPolylineWeight = (emissions) => {
    if (!emissions) return 3;
    if (emissions > 5000) return 5;
    if (emissions > 2000) return 4;
    return 3;
  };

  return (
    <div className={styles.card}>
      <div className={styles.mapContainer}>
        <MapContainer center={defaultCenter} zoom={2} style={{ width: '100%', height: '100%' }}>
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {validShipments.map((s) => {
            const origin = [Number(s.originLat), Number(s.originLng)];
            const destination = [Number(s.destinationLat), Number(s.destinationLng)];
            const emissions = s.totalEmissionsKgco2e;

            return (
              <div key={s.id}>
                {/* Origin Marker */}
                <Marker position={origin}>
                  <Popup>
                    <div className={styles.popupContent}>
                      <span className={styles.popupTitle}>Origin: {s.originCity || 'Origin'}</span>
                      <span>Country: {s.originCountry || 'N/A'}</span>
                      <span>Shipment #{s.id} ({s.invoiceNumber})</span>
                    </div>
                  </Popup>
                </Marker>

                {/* Destination Marker */}
                <Marker position={destination}>
                  <Popup>
                    <div className={styles.popupContent}>
                      <span className={styles.popupTitle}>Destination: {s.destinationCity || 'Destination'}</span>
                      <span>Country: {s.destinationCountry || 'N/A'}</span>
                      <span>Shipment #{s.id} ({s.invoiceNumber})</span>
                    </div>
                  </Popup>
                </Marker>

                {/* Route Polyline */}
                <Polyline
                  positions={[origin, destination]}
                  color={getPolylineColor(emissions)}
                  weight={getPolylineWeight(emissions)}
                  opacity={0.8}
                >
                  <Popup>
                    <div className={styles.popupContent}>
                      <span className={styles.popupTitle}>
                        Invoice: {s.invoiceNumber || `#${s.id}`}
                      </span>
                      <span>
                        Route: {s.originCity} &rarr; {s.destinationCity}
                      </span>
                      <span>Mode: {s.transportMode}</span>
                      <span>
                        Distance: {s.distanceKm ? `${formatNumber(s.distanceKm, 0)} km` : 'N/A'}
                      </span>
                      <span className={styles.popupValue}>
                        Emissions: {formatEmissions(emissions)}
                      </span>
                    </div>
                  </Popup>
                </Polyline>
              </div>
            );
          })}
        </MapContainer>
      </div>
    </div>
  );
}

export default EmissionsMap;
