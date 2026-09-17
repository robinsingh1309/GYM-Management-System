import axiosClient from './axiosClient';

export const getMembers = () => {
  return axiosClient.get('/api/v1/members');
};

export const getMemberById = (id) => {
  return axiosClient.get(`/api/v1/members/${id}`);
};

export const createMember = (data) => {
  return axiosClient.post('/api/v1/members', data);
};

export const activateMember = (id) => {
  return axiosClient.patch(`/api/v1/members/${id}/activate`);
};

export const deactivateMember = (id) => {
  return axiosClient.patch(`/api/v1/members/${id}/deactivate`);
};