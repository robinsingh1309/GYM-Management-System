export const formatMembershipType = (membershipType) => {
  const labels = {
    MONTHLY: 'Monthly',
    QUARTERLY: 'Quarterly',
    HALF_YEARLY: 'Half Yearly',
    YEARLY: 'Yearly',
  };

  return labels[membershipType] || membershipType;
};

export const formatMembershipStatus = (status) => {
  const labels = {
    ACTIVE: 'Active',
    UPCOMING: 'Upcoming',
    EXPIRED: 'Expired',
    INACTIVE: 'Inactive',
  };

  return labels[status] || status;
};

export const formatPaymentStatus = (paymentStatus) => {
  const labels = {
    PAID: 'Paid',
    PARTIALLY_PAID: 'Partially Paid',
  };

  return labels[paymentStatus] || paymentStatus;
};

export const calculateMembershipSummary = (memberships) => {
  return {
    total: memberships.length,

    active: memberships.filter(
      (membership) => membership.status === 'ACTIVE'
    ).length,

    upcoming: memberships.filter(
      (membership) => membership.status === 'UPCOMING'
    ).length,

    expired: memberships.filter(
      (membership) => membership.status === 'EXPIRED'
    ).length,

    outstandingAmount: memberships.reduce(
      (total, membership) =>
        total + Number(membership.outstandingAmount || 0),
      0
    ),
  };
};