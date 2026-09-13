import axiosClient from './axiosClient';

export const getMembers = () => {
  return axiosClient.get('/api/v1/members');
};

export const getMemberById = (id) => {
  return axiosClient.get(`/api/v1/members/${id}`);
};