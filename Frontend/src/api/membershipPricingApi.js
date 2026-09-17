import axiosClient from './axiosClient';

export const getMembershipPricings = () => {
  return axiosClient.get(`/api/v1/membership-pricing`);
};