// NOTE: I don't have your `PaymentMode` enum definition, so these are a
// best guess at common values. Please align this list (and the `colors`
// map below) with the actual enum in `com.example.fitmanager.entity.PaymentMode`.
export const PAYMENT_MODE_OPTIONS = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CARD', label: 'Card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
  { value: 'CHEQUE', label: 'Cheque' },
  { value: 'ONLINE', label: 'Online' },
];

export const formatPaymentMode = (value) => {
  if (!value) {
    return '';
  }

  return value
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const getPaymentModeColor = (value) => {
  const colors = {
    CASH: 'green',
    CARD: 'blue',
    UPI: 'purple',
    BANK_TRANSFER: 'geekblue',
    CHEQUE: 'gold',
    ONLINE: 'cyan',
  };

  return colors[value] || 'default';
};