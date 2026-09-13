import { formatMembershipType, formatMembershipStatus, formatPaymentStatus } from './membershipUtils';


export const calculateTotalMembers = (members) => {
  return members.length;
};

export const calculateActiveMembers = (members) => {
  return members.filter(
    (member) => member.active
  ).length;
};

export const calculateActiveMemberships = (memberships) => {
  return memberships.filter(
    (membership) => membership.status === 'ACTIVE'
  ).length;
};

export const calculateOutstandingAmount = (memberships) => {
  return memberships.reduce(
    (total, membership) =>
      total + Number(membership.outstandingAmount || 0),
    0
  );
};

export const getRecentMemberships = (memberships, members) => {
  return memberships
    .slice()
    .sort(
      (a, b) =>
        new Date(b.updatedAt) - new Date(a.updatedAt)
    )
    .slice(0, 5)
    .map((membership) => {
      const member = members.find(
        (member) => member.id === membership.memberId
      );

      return {
        key: membership.id,
        memberId: membership.memberId,
        member: member
          ? member.name
          : `Member #${membership.memberId}`,
        membership: formatMembershipType(membership.membershipType),
        startDate: membership.startDate,
        endDate: membership.endDate,
        status: formatMembershipStatus(membership.status),
        paymentStatus: formatPaymentStatus(membership.paymentStatus),
        outstandingAmount: membership.outstandingAmount,
        expiringSoon: isMembershipExpiringSoon(
          membership.endDate,
          membership.status
        ),
      };
    });
};

const getLocalMidnight = (dateInput) => {
  if (!dateInput) return null;

  let date;
  if (typeof dateInput === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateInput)) {
    const [year, month, day] = dateInput.split('-').map(Number);
    date = new Date(year, month - 1, day);
  } else {
    date = new Date(dateInput);
  }

  if (Number.isNaN(date.getTime())) return null;

  date.setHours(0, 0, 0, 0);
  return date;
};

export const isMembershipExpiringSoon = (endDate, status) => {
  // Upcoming memberships are not expiring soon
  if (status?.toUpperCase() === 'UPCOMING') {
    return false;
  }

  const expiryDate = getLocalMidnight(endDate);
  if (!expiryDate) return false;

  const today = getLocalMidnight(new Date());

  const differenceInDays = Math.round(
    (expiryDate - today) / (1000 * 60 * 60 * 24)
  );

  return differenceInDays >= 0 && differenceInDays <= 7;
};

export const getMembershipExpiryText = (endDate, status) => {
  if (status?.toUpperCase() === 'UPCOMING') {
    return 'Upcoming';
  }

  const expiryDate = getLocalMidnight(endDate);
  if (!expiryDate) return '-';

  const today = getLocalMidnight(new Date());

  const differenceInDays = Math.round(
    (expiryDate - today) / (1000 * 60 * 60 * 24)
  );

  if (differenceInDays < 0) return 'Expired';
  if (differenceInDays === 0) return 'Expires today';
  if (differenceInDays === 1) return 'Expires in 1 day';

  return `Expires in ${differenceInDays} days`;
};