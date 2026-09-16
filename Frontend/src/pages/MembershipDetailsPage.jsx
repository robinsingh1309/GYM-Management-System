import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Alert, Breadcrumb, Button, Card, Descriptions, Empty, Spin, Table, Tag, } from 'antd';

import { getMembershipById } from '../api/membershipApi';
import { getPaymentsByMembershipId } from '../api/paymentApi';

import {
  formatMembershipType,
  formatMembershipStatus,
  formatPaymentStatus,
} from '../utils/membershipUtils';

import { formatCurrency } from '../utils/currencyUtils';
import { formatDate } from '../utils/dateUtils';

function MembershipDetailsPage() {
    const navigate = useNavigate();

    const { id } = useParams();

    const [membership, setMembership] = useState(null);

    const [payments, setPayments] = useState([]);
    const [paymentsLoading, setPaymentsLoading] = useState(true);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
      setLoading(true);
      setError(null);
      setPaymentsLoading(true);

      getMembershipById(id)
        .then((response) => {
          setMembership(response.data);
          return getPaymentsByMembershipId(response.data.id);
        })
        .then((response) => {
          setPayments(response.data);
        })
        .catch((error) => {
          setError('Failed to load membership details.');
        })
        .finally(() => {
          setLoading(false);
          setPaymentsLoading(false);
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
    const paymentColumns = [
    {
      title: 'Payment Date',
      dataIndex: 'paymentDate',
      key: 'paymentDate',
      render: (value) => formatDate(value),
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (value, record) => (
        <Button type="link" style={{ padding: 0 }}
          onClick={() =>
            navigate(`/payments/${record.id}`)
          }
        >
          {formatCurrency(value)}
        </Button>
      ),
    },
    {
      title: 'Payment Mode',
      dataIndex: 'paymentMode',
      key: 'paymentMode',
      render: (value) => value || '-',
    },
  ];

  return (
    <div>
      <Breadcrumb items={[
          { title: 'Home', onClick: () => navigate('/dashboard') },
          { title: `Members #${membership.memberId}`, onClick: () => navigate(`/members/${membership.memberId}`),},
          { title: `Membership Details #${membership.id}`,},
        ]}
      />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, marginBottom: 24, }}>
        <div>
          <h2 style={{ margin: 0 }}>
            Membership #{membership.id}
          </h2>
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
      <Card title="Payment History" style={{ marginTop: 24 }}>
        {paymentsLoading ? (
          <Spin />
        ) : payments.length > 0 ? (
          <Table
            columns={paymentColumns}
            dataSource={payments}
            rowKey="id"
            pagination={false}
          />
        ) : (
          <Empty description="No payments found" />
        )}
      </Card>
    </div>
  );
}

export default MembershipDetailsPage;