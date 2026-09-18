import axiosClient from './axiosClient';

export const createMembershipPricing = (payload) => {
  return axiosClient.post(`/api/v1/membership-pricing`, payload);
};

export const getMembershipPricings = () => {
  return axiosClient.get(`/api/v1/membership-pricing`);
};

export const getPricingById = (id) => {
  return axiosClient.get(`/api/v1/membership-pricing/${id}`);
};

export const getPricingByMembershipType = (membershipType) => {
  return axiosClient.get(`/api/v1/membership-pricing/type/${membershipType}`);
};

export const activateMembershipPricing = (id) => {
  return axiosClient.patch(`/api/v1/membership-pricing/${id}/activate`);
};

export const deactivateMembershipPricing = (id) => {
  return axiosClient.patch(`/api/v1/membership-pricing/${id}/deactivate`);
};