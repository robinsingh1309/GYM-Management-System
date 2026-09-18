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
        setPayment(response.data);
      })
      .catch((error) => {
        if (error.response?.status === 404) {
          setError(null);
        } else {
          setError('Failed to load payments details.');
        }
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
      <div style={{ marginTop: 16 }}>
        <Alert type="warning" message="Payment not found." showIcon style={{ marginBottom: 16 }}/>

        <Button onClick={() => navigate('/payments')}>
          Back to Payments
        </Button>
      </div>
    );
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: 'Home', onClick: () => navigate('/dashboard'), },
          { title: 'Members', onClick: () => navigate('/members'), },
          { title: `Member #${payment.memberId}`, onClick: () => navigate(`/members/${payment.memberId}`), },
          { title: `Payment #${payment.id}`, },
        ]}
      />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, marginBottom: 24, }}>
        <div>
          <h2 style={{ margin: 0 }}>
            Payment #{payment.id}
          </h2>
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
            <Button type="link" style={{ padding: 0 }}
              onClick={() =>
                navigate(
                  `/memberships/${payment.membershipId}`
                )
              }
            >
              Membership #{payment.membershipId}
            </Button>
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