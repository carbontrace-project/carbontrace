/**
 * Formats a number with standard thousands separators.
 * @param {number|string} value - The number to format
 * @param {number} decimals - Number of decimal places
 * @returns {string} Formatted number string
 */
export function formatNumber(value, decimals = 2) {
  if (value === null || value === undefined || isNaN(Number(value))) {
    return 'N/A';
  }
  const num = Number(value);
  return num.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

/**
 * Formats emissions values in kgCO₂e with thousands separators.
 * @param {number|string} kg - Total emissions in kgCO₂e
 * @returns {string} Formatted string with kgCO₂e suffix
 */
export function formatEmissions(kg) {
  if (kg === null || kg === undefined || isNaN(Number(kg))) {
    return '0.00 kgCO₂e';
  }
  return `${formatNumber(kg, 2)} kgCO₂e`;
}

/**
 * Formats currency in USD with 2 decimal places.
 * @param {number|string} amount - Currency amount in USD
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount) {
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return '$0.00';
  }
  const num = Number(amount);
  return num.toLocaleString('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

/**
 * Formats weight in tonnes to 3 decimal places.
 * @param {number|string} tonnes - Weight in tonnes
 * @param {boolean} shortUnit - If true, uses 't', else 'tonnes'
 * @returns {string} Formatted tonnes string
 */
export function formatTonnes(tonnes, shortUnit = false) {
  if (tonnes === null || tonnes === undefined || isNaN(Number(tonnes))) {
    return `0.000 ${shortUnit ? 't' : 'tonnes'}`;
  }
  const unitStr = shortUnit ? 't' : 'tonnes';
  return `${formatNumber(tonnes, 3)} ${unitStr}`;
}
