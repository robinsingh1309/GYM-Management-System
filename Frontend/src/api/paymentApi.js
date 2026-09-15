import axiosClient from './axiosClient';

export const getPaymentsByMemberId = (memberId) => {
  return axiosClient.get(`/api/v1/payments/member/${memberId}`);
};

export const getPaymentById = (id) => {
  return axiosClient.get(`/api/v1/payments/${id}`);
};