import axiosClient from './axiosClient';

export const getMembershipsByMemberId = (memberId) => {
  return axiosClient.get(`/api/v1/memberships/member/${memberId}`);
};

export const getMembershipById = (id) => {
  return axiosClient.get(`/api/v1/memberships/${id}`);
};

export const getMemberships = () => {
  return axiosClient.get('/api/v1/memberships');
};

export const createMembership = (payload) => {
  return axiosClient.post('/api/v1/memberships', payload);
};

export const activateMembership = (id) => {
  return axiosClient.patch(`/api/v1/memberships/${id}/activate`);
};

export const deactivateMembership = (id) => {
  return axiosClient.patch(`/api/v1/memberships/${id}/deactivate`);
};