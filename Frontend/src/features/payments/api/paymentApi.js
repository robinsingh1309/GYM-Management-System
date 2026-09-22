import axiosClient from '../../../api/axiosClient';

export const getPayments = () => {
  return axiosClient.get(`/api/v1/payments`);
};

export const getPaymentById = (id) => {
  return axiosClient.get(`/api/v1/payments/${id}`);
};

export const createPayment = (payload) => {
  return axiosClient.post('/api/v1/payments', payload);
};

export const getPaymentsByMemberId = (memberId) => {
  return axiosClient.get(`/api/v1/payments/member/${memberId}`);
};

export const getPaymentsByMembershipId = (membershipId) => {
  return axiosClient.get(`/api/v1/payments/membership/${membershipId}`);
};