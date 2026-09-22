import {
  Alert,
  Breadcrumb,
  Button,
  Card,
  Descriptions,
  Col,
  Empty,
  Row,
  Space,
  Spin,
  Table,
  Tag,
} from 'antd';

import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  DollarOutlined,
  TeamOutlined,
} from '@ant-design/icons';

import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { getMemberById } from '../api/memberApi';
import { getMembershipsByMemberId } from '../../memberships/api/membershipApi';
import { getPaymentsByMemberId } from '../../payments/api/paymentApi';

import {
  formatPaymentStatus,
  formatMembershipStatus,
  formatMembershipType,
  calculateMembershipSummary,
} from '../../../utils/membershipUtils';

import { formatCurrency } from '../../../utils/currencyUtils';
import { formatDate } from '../../../utils/dateUtils';

function MemberDetailsPage() {
  const navigate = useNavigate();
  const { id } = useParams();

  // ─────────────────────────────────────────────
  // Data
  // ─────────────────────────────────────────────

  const [member, setMember] = useState(null);
  const [memberships, setMemberships] = useState([]);
  const [payments, setPayments] = useState([]);

  // ─────────────────────────────────────────────
  // Page loading / error
  // ─────────────────────────────────────────────

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ─────────────────────────────────────────────
  // Section-specific errors
  // ─────────────────────────────────────────────

  const [membershipsError, setMembershipsError] = useState(null);
  const [paymentsError, setPaymentsError] = useState(null);

  // ─────────────────────────────────────────────
  // Derived membership summary
  // ─────────────────────────────────────────────

  const membershipSummary = calculateMembershipSummary(memberships);

  // ─────────────────────────────────────────────
  // Load data
  // ─────────────────────────────────────────────

  useEffect(() => {
    const loadMemberDetails = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await getMemberById(id);
        setMember(response.data);
      } catch (error) {
        setMember(null);

        if (error.response?.status === 404) {
          setError(null);
        } else {
          setError('Failed to load member details.');
        }
      } finally {
        setLoading(false);
      }
    };

    const loadMembershipHistory = async () => {
      setMembershipsError(null);

      try {
        const response = await getMembershipsByMemberId(id);
        setMemberships(response.data);
      } catch (error) {
        setMemberships([]);
        setMembershipsError('Failed to load membership history.');
      }
    };

    const loadPaymentHistory = async () => {
      setPaymentsError(null);

      try {
        const response = await getPaymentsByMemberId(id);
        setPayments(response.data);
      } catch (error) {
        setPayments([]);
        setPaymentsError('Failed to load payment history.');
      }
    };

    loadMemberDetails();
    loadMembershipHistory();
    loadPaymentHistory();
  }, [id]);

  // ─────────────────────────────────────────────
  // Page-level loading
  // ─────────────────────────────────────────────

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }

  // ─────────────────────────────────────────────
  // Page-level error
  // ─────────────────────────────────────────────

  if (error) {
    return (
      <div>
        <Alert
          type="error"
          message={error}
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Button onClick={() => navigate('/members')}>
          Back to Members
        </Button>
      </div>
    );
  }

  // ─────────────────────────────────────────────
  // Member not found
  // ─────────────────────────────────────────────

  if (!member) {
    return (
      <div>
        <Alert
          type="warning"
          message="Member not found."
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Button onClick={() => navigate('/members')}>
          Back to Members
        </Button>
      </div>
    );
  }

  // ─────────────────────────────────────────────
  // Membership table columns
  // ─────────────────────────────────────────────

  const membershipColumns = [
    {
      title: 'Membership Type',
      dataIndex: 'membershipType',
      key: 'membershipType',
      render: (value, record) => (
        <Button
          type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/memberships/${record.id}`)}
        >
          {formatMembershipType(value)}
        </Button>
      ),
    },
    {
      title: 'Start Date',
      dataIndex: 'startDate',
      key: 'startDate',
      render: (date) => formatDate(date),
    },
    {
      title: 'End Date',
      dataIndex: 'endDate',
      key: 'endDate',
      render: (date) => formatDate(date),
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => formatCurrency(amount),
    },
    {
      title: 'Total Paid',
      dataIndex: 'totalPaid',
      key: 'totalPaid',
      render: (totalPaid) => formatCurrency(totalPaid),
    },
    {
      title: 'Outstanding',
      dataIndex: 'outstandingAmount',
      key: 'outstandingAmount',
      render: (outstandingAmount) => formatCurrency(outstandingAmount),
    },
    {
      title: 'Payment Status',
      dataIndex: 'paymentStatus',
      key: 'paymentStatus',
      render: (paymentStatus) => {
        const formatted = formatPaymentStatus(paymentStatus);

        const colorMap = {
          Paid: 'green',
          'Partially Paid': 'orange',
        };

        return (
          <Tag color={colorMap[formatted] || 'default'}>
            {formatted}
          </Tag>
        );
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const formatted = formatMembershipStatus(status);

        const colorMap = {
          Active: 'green',
          Upcoming: 'blue',
          Expired: 'red',
          Inactive: 'default',
        };

        return (
          <Tag color={colorMap[formatted] || 'default'}>
            {formatted}
          </Tag>
        );
      },
    },
  ];

  // ─────────────────────────────────────────────
  // Payment table columns
  // ─────────────────────────────────────────────

  const paymentColumns = [
    {
      title: 'Payment Date',
      dataIndex: 'paymentDate',
      key: 'paymentDate',
      render: (value) => formatDate(value),
    },
    {
      title: 'Membership',
      dataIndex: 'membershipId',
      key: 'membershipId',
      render: (value) => `Membership #${value}`,
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (value, record) => (
        <Button
          type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/payments/${record.id}`)}
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
      {/* ───────────────────────────────────── */}
      {/* Breadcrumb / Navigation */}
      {/* ───────────────────────────────────── */}

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Breadcrumb
          items={[
            {
              title: 'Home',
              onClick: () => navigate('/dashboard'),
            },
            {
              title: 'Members',
              onClick: () => navigate('/members'),
            },
            {
              title: `Member Details #${member.id}`,
            },
          ]}
        />

        <Button onClick={() => navigate('/members')}>
          ← Back to Members
        </Button>
      </div>

      {/* ───────────────────────────────────── */}
      {/* Page Title */}
      {/* ───────────────────────────────────── */}

      <div style={{ marginTop: 16 }}>
        <h1 style={{ marginBottom: 4 }}>
          {member.name}
        </h1>
      </div>

      {/* ───────────────────────────────────── */}
      {/* Member Information */}
      {/* ───────────────────────────────────── */}

      <Card>
        <Descriptions bordered column={1}>
          <Descriptions.Item label="Name">
            {member.name}
          </Descriptions.Item>

          <Descriptions.Item label="Email">
            {member.email}
          </Descriptions.Item>

          <Descriptions.Item label="Phone Number">
            {member.phoneNumber}
          </Descriptions.Item>

          <Descriptions.Item label="Date of Birth">
            {member.dateOfBirth
              ? formatDate(member.dateOfBirth)
              : '-'}
          </Descriptions.Item>

          <Descriptions.Item label="Gender">
            {member.gender || '-'}
          </Descriptions.Item>

          <Descriptions.Item label="Joining Date">
            {member.joiningDate
              ? formatDate(member.joiningDate)
              : '-'}
          </Descriptions.Item>

          <Descriptions.Item label="Address">
            {member.address || '-'}
          </Descriptions.Item>

          <Descriptions.Item label="Status">
            <Tag color={member.active ? 'green' : 'red'}>
              {member.active ? 'Active' : 'Inactive'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* ───────────────────────────────────── */}
      {/* Membership Summary */}
      {/* ───────────────────────────────────── */}

      {membershipsError ? (
        <Alert
          type="error"
          message="Membership summary could not be loaded because membership history failed to load."
          showIcon
          style={{ marginTop: 24 }}
        />
      ) : (
        <Row
          gutter={[16, 16]}
          style={{ marginTop: 24 }}
        >
          <Col xs={24} sm={12} md={8} lg={4}>
            <Card>
              <Space align="start">
                <TeamOutlined
                  style={{ fontSize: 24 }}
                />

                <div>
                  <div>Total Memberships</div>

                  <h2 style={{ margin: '4px 0 0' }}>
                    {membershipSummary.total}
                  </h2>
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8} lg={4}>
            <Card>
              <Space align="start">
                <CheckCircleOutlined
                  style={{
                    fontSize: 24,
                    color: '#52c41a',
                  }}
                />

                <div>
                  <div>Active Memberships</div>

                  <h2 style={{ margin: '4px 0 0' }}>
                    {membershipSummary.active}
                  </h2>
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8} lg={4}>
            <Card>
              <Space align="start">
                <ClockCircleOutlined
                  style={{
                    fontSize: 24,
                    color: '#1677ff',
                  }}
                />

                <div>
                  <div>Upcoming Memberships</div>

                  <h2 style={{ margin: '4px 0 0' }}>
                    {membershipSummary.upcoming}
                  </h2>
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8} lg={4}>
            <Card>
              <Space align="start">
                <ExclamationCircleOutlined
                  style={{
                    fontSize: 24,
                    color: '#ff4d4f',
                  }}
                />

                <div>
                  <div>Expired Memberships</div>

                  <h2 style={{ margin: '4px 0 0' }}>
                    {membershipSummary.expired}
                  </h2>
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8} lg={8}>
            <Card>
              <Space align="start">
                <DollarOutlined
                  style={{
                    fontSize: 24,
                    color: '#faad14',
                  }}
                />

                <div>
                  <div>Outstanding Amount</div>

                  <h2 style={{ margin: '4px 0 0' }}>
                    {formatCurrency(
                      membershipSummary.outstandingAmount
                    )}
                  </h2>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>
      )}

      {/* ───────────────────────────────────── */}
      {/* Membership History */}
      {/* ───────────────────────────────────── */}

      <Card
        title="Membership History"
        style={{ marginTop: 24 }}
      >
        {membershipsError ? (
          <Alert
            type="error"
            message={membershipsError}
            showIcon
          />
        ) : memberships.length > 0 ? (
          <Table
            columns={membershipColumns}
            dataSource={memberships}
            rowKey="id"
            pagination={false}
            scroll={{ x: 900 }}
          />
        ) : (
          <Empty description="No membership history yet" />
        )}
      </Card>

      {/* ───────────────────────────────────── */}
      {/* Payment History */}
      {/* ───────────────────────────────────── */}

      <Card
        title="Payment History"
        style={{ marginTop: 24 }}
      >
        {paymentsError ? (
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
            scroll={{ x: 700 }}
          />
        ) : (
          <Empty description="No payments found" />
        )}
      </Card>
    </div>
  );
}

export default MemberDetailsPage;