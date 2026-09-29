import axiosClient from '../../../api/axiosClient';
import { CHAT_ENDPOINT } from '../constants';

export const sendChatQuestion = (question) => {
  return axiosClient.post(CHAT_ENDPOINT, { question });
};
