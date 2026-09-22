import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Alert, Breadcrumb, Button, Card, Descriptions, Empty, Spin, Table, Tag, } from 'antd';

import { getMembershipById } from '../api/membershipApi';
import { getPaymentsByMembershipId } from '../../payments/api/paymentApi';

import {
  formatMembershipType,
  formatMembershipStatus,
  formatPaymentStatus,
} from '../../../utils/membershipUtils';

import { formatCurrency } from '../../../utils/currencyUtils';
import { formatDate } from '../../../utils/dateUtils';

function MembershipDetailsPage() {
    const navigate = useNavigate();

    const { id } = useParams();

    const [membership, setMembership] = useState(null);

    const [payments, setPayments] = useState([]);
    const [paymentsLoading, setPaymentsLoading] = useState(true);

    const [paymentsError, setPaymentsError] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
      const loadMembershipDetails = async () => {
        setLoading(true);
        setError(null);

        try {
          const response = await getMembershipById(id);
          setMembership(response.data);
        } catch (error) {
          setMembership(null);

          if (error.response?.status === 404) {
            setError(null);
          } else {
            setError('Failed to load memberships details.');
          }
        } finally {
          setLoading(false);
        }
      };

      const loadPayments = async () => {
        setPaymentsLoading(true);
        setPaymentsError(null);

        try {
          const response = await getPaymentsByMembershipId(id);
          setPayments(response.data);
        } catch (error) {
          setPayments([]);
          setPaymentsError('Failed to load payment history.');
        } finally {
          setPaymentsLoading(false);
        }
      };

      loadMembershipDetails();
      loadPayments();
    }, [id]);

    if (loading) { 
      return <Spin size="large" />; 
    }

    if (error) {
      return <Alert type="error" message={error} showIcon />; 
    }

    if (!membership) {
      return (
        <div>
          <Alert type="warning" message="Membership not found." showIcon style={{ marginBottom: 16 }}/>

          <Button onClick={() => navigate('/memberships')}>
            Back to Memberships
          </Button>
        </div>
      );
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
        ) : paymentsError ? (
          <Alert
            type="error"
            message={paymentsError}
            showIcon
          />
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