import axiosClient from './axiosClient';

export const getMembershipsByMemberId = (memberId) => {
  return axiosClient.get(`/api/v1/memberships/member/${memberId}`);
};