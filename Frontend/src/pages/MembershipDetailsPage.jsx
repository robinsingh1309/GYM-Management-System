import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Alert, Breadcrumb, Button, Card, Descriptions, Spin, Tag, } from 'antd';

import { getMembershipById } from '../api/membershipApi';
import { formatMembershipType, formatMembershipStatus, formatPaymentStatus,} from '../utils/membershipUtils';
import { formatCurrency } from '../utils/currencyUtils';
import { formatDate } from '../utils/dateUtils';

function MembershipDetailsPage() {
    const navigate = useNavigate();

    const { id } = useParams();

    const [membership, setMembership] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
    setLoading(true);
    setError(null);

    getMembershipById(id)
        .then((response) => {
            setMembership(response.data);
        })
        .catch((error) => {
            setError('Failed to load membership details.');
        })
        .finally(() => {
            setLoading(false);
        });
    }, [id]);

    if (loading) { 
        return <Spin size="large" />; 
    }

    if (error) {
        return <Alert type="error" title={error} />; 
    }

    if (!membership) { 
        return <Alert type="warning" title="Membership not found." />;
    }

    const getMembershipStatusColor = (status) => {
        switch (status) {
            case 'ACTIVE':
                return 'green';
            case 'UPCOMING':
                return 'blue';
            case 'EXPIRED':
                return 'red';
            case 'INACTIVE':
                return 'default';
            default:
                return 'default';
        }
    };

    const getPaymentStatusColor = (status) => {
        switch (status) {
            case 'PAID':
                return 'green';
            case 'PARTIALLY_PAID':
                return 'orange';
            case 'UNPAID':
                return 'red';
            default:
                return 'default';
        }
    };

  return (
    <div>
      <Breadcrumb items={[{ title: 'Home' }, { title: 'Members', onClick: () => navigate('/members'),}, {title: 'Membership Details',},]}/>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, marginBottom: 24, }}>
        <div>
          <h2 style={{ margin: 0 }}>
            Membership #{membership.id}
          </h2>

          <div style={{ marginTop: 4 }}>
            Member #{membership.memberId}
          </div>
        </div>

        <Button onClick={() => navigate(-1)}>
          Back
        </Button>
      </div>

      <Card title="Membership Details">
        <Descriptions bordered column={1}>
          <Descriptions.Item label="Membership Type">
            {formatMembershipType(
              membership.membershipType
            )}
          </Descriptions.Item>

          <Descriptions.Item label="Start Date">
            {formatDate(membership.startDate)}
          </Descriptions.Item>

          <Descriptions.Item label="End Date">
            {formatDate(membership.endDate)}
          </Descriptions.Item>

          <Descriptions.Item label="Status">
            <Tag color={getMembershipStatusColor(membership.status)}>
              {formatMembershipStatus(membership.status)}
            </Tag>
          </Descriptions.Item>

          <Descriptions.Item label="Membership Amount">
            {formatCurrency(membership.amount)}
          </Descriptions.Item>

          <Descriptions.Item label="Total Paid">
            {formatCurrency(membership.totalPaid)}
          </Descriptions.Item>

          <Descriptions.Item label="Outstanding Amount">
            {formatCurrency(
              membership.outstandingAmount
            )}
          </Descriptions.Item>

          <Descriptions.Item label="Payment Status">
            <Tag color={getPaymentStatusColor(membership.paymentStatus)}>
              {formatPaymentStatus(membership.paymentStatus)}
            </Tag>
          </Descriptions.Item>

          <Descriptions.Item label="Active">
            {membership.active ? 'Yes' : 'No'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}

export default MembershipDetailsPage;