import { useEffect } from 'react';
import { getMembers } from '../api/memberApi';

function MembersPage() {
  useEffect(() => {
    getMembers()
      .then((response) => {
        console.log('Members:', response.data);
      })
      .catch((error) => {
        console.error('Failed to fetch members:', error);
      });
  }, []);

  return <h1>Members Page</h1>;
}

export default MembersPage;