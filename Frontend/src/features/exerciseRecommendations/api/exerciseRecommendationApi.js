import axiosClient from '../../../api/axiosClient';

export const recommendExercises = (payload) => {
  return axiosClient.post('/api/v1/exercises/qdrant/recommend', payload);
};
