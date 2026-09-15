import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Alert, Breadcrumb, Button, Card, Descriptions, Spin,} from 'antd';

import { getPaymentById } from '../api/paymentApi';

import { formatCurrency } from '../utils/currencyUtils';
import { formatDate } from '../utils/dateUtils';

function PaymentDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    getPaymentById(id)
      .then((response) => {
        console.log('Payment:', response.data);
        setPayment(response.data);
      })
      .catch((error) => {
        console.error('Failed to fetch payment:', error);
        setError('Failed to load payment details.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [id]);

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" message={error} />;
  }

  if (!payment) {
    return (
      <Alert
        type="warning"
        message="Payment not found."
      />
    );
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home' },
          {
            title: 'Members',
            onClick: () => navigate('/members'),
          },
          {
            title: 'Payment Details',
          },
        ]}
      />

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginTop: 16,
          marginBottom: 24,
        }}
      >
        <div>
          <h2 style={{ margin: 0 }}>
            Payment #{payment.id}
          </h2>

          <div style={{ marginTop: 4 }}>
            Member #{payment.memberId}
          </div>
        </div>

        <Button onClick={() => navigate(-1)}>
          Back
        </Button>
      </div>

      <Card title="Payment Details">
        <Descriptions bordered column={1}>
          <Descriptions.Item label="Payment ID">
            {payment.id}
          </Descriptions.Item>

          <Descriptions.Item label="Member ID">
            {payment.memberId}
          </Descriptions.Item>

          <Descriptions.Item label="Membership ID">
            {payment.membershipId}
          </Descriptions.Item>

          <Descriptions.Item label="Amount">
            {formatCurrency(payment.amount)}
          </Descriptions.Item>

          <Descriptions.Item label="Payment Date">
            {formatDate(payment.paymentDate)}
          </Descriptions.Item>

          <Descriptions.Item label="Payment Mode">
            {payment.paymentMode || '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}

export default PaymentDetailsPage;