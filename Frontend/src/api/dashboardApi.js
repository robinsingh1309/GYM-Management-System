import axiosClient from './axiosClient';

export const getDashboardData = async () => {
  const [membersResponse, membershipsResponse, paymentsResponse] =
    await Promise.all([
      axiosClient.get('/api/v1/members'),
      axiosClient.get('/api/v1/memberships'),
      axiosClient.get('/api/v1/payments'),
    ]);

  return {
    members: membersResponse.data,
    memberships: membershipsResponse.data,
    payments: paymentsResponse.data,
  };
};