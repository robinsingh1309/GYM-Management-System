import { ReloadOutlined, UserAddOutlined, IdcardOutlined, DollarOutlined, } from '@ant-design/icons';
import { Alert, Badge, Breadcrumb, Button, Card, Col, Empty, Row, Space, Spin, Table, Tag, Typography } from 'antd';

import { getDashboardData } from '../api/dashboardApi';
import { calculateTotalMembers, calculateActiveMembers, calculateActiveMemberships, calculateOutstandingAmount, getRecentMemberships, getMembershipExpiryText} from '../utils/dashboardUtils';
import { formatCurrency } from '../utils/currencyUtils';
import { formatMembershipStatus, formatPaymentStatus } from '../utils/membershipUtils';
import { formatDate } from '../utils/dateUtils';

import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';

const { Title, Text } = Typography;

function DashboardPage() {

  const { user } = useAuth();
  const getDisplayName = (subject) => {
    if (!subject) {
      return 'User';
    }
    const name = subject.split('@')[0];
    return name.charAt(0).toUpperCase() + name.slice(1);
  };

  const displayName = getDisplayName(user?.sub);

  const navigate = useNavigate();

  const [members, setMembers] = useState([]);
  const [memberships, setMemberships] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getDashboardData();

      setMembers(data.members || []);
      setMemberships(data.memberships || []);
    } catch (err) {
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const totalMembers = calculateTotalMembers(members);
  const activeMembers = calculateActiveMembers(members);
  const activeMemberships = calculateActiveMemberships(memberships);
  const outstandingAmount = calculateOutstandingAmount(memberships);

  const recentMembershipColumns = [
    {
      title: 'Member',
      dataIndex: 'member',
      key: 'member',
      render: (name, record) => (
        <div onClick={() => navigate(`/members/${record.memberId}`)} style={{ cursor: 'pointer' }}>
          <Text strong>{name}</Text>
        </div>
      ),
    },
    {
      title: 'Membership',
      dataIndex: 'membership',
      key: 'membership',
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
      title: 'Expiry',
      dataIndex: 'endDate',
      key: 'expiry',
      render: (endDate, record) => {
        const expiryText = getMembershipExpiryText(endDate, record.status);

        if (expiryText === 'Expired') {
          return <Badge status="error" text={expiryText} />;
        }

        if (expiryText === 'Expires today') {
          return <Badge status="warning" text={expiryText} />;
        }

        if (expiryText.startsWith('Expires in')) {
          return <Badge status="warning" text={expiryText} />;
        }

        return <span>-</span>;
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const colorMap = {
          Active: 'green',
          Upcoming: 'blue',
          Expired: 'red',
          Inactive: 'default',
        };

        return <Tag color={colorMap[status] || 'default'}>{formatMembershipStatus(status)}</Tag>;
      },
    },
    {
      title: 'Payment Status',
      dataIndex: 'paymentStatus',
      key: 'paymentStatus',
      render: (paymentStatus) => {
        const colorMap = {
          Paid: 'green',
          'Partially Paid': 'orange',
        };

        return <Tag color={colorMap[paymentStatus] || 'default'}>{formatPaymentStatus(paymentStatus)}</Tag>;
      },
    },
    {
      title: 'Outstanding',
      dataIndex: 'outstandingAmount',
      key: 'outstandingAmount',
      render: (amount) => formatCurrency(amount),
    },
  ];

  const recentMembershipData = getRecentMemberships(memberships, members);

  return (
    <div>
      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px' }}>
          <Spin size="large" />
        </div>
      ) : error ? (<Alert title="Unable to load dashboard" description={error} type="error" showIcon />) : (
        <>
          <Space orientation="vertical" size={4} style={{ width: '100%' }}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap',gap: 12,}}>
              <Breadcrumb items={[{title: 'Home',}, {title: 'Dashboard',},]}/>

              <Button icon={<ReloadOutlined />} onClick={loadDashboardData} loading={loading}>
                Refresh
              </Button>
            </div>

            <Text type="secondary">
              Welcome, {displayName}! Here's an overview of your gym.
            </Text>
          </Space>

          <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
            <Col xs={24} sm={12} lg={6}>
              <Card title="Total Members">
                <Title level={3}>{totalMembers}</Title>
              </Card>
            </Col>

            <Col xs={24} sm={12} lg={6}>
              <Card title="Active Members">
                <Title level={3}>{activeMembers}</Title>
              </Card>
            </Col>

            <Col xs={24} sm={12} lg={6}>
              <Card title="Active Memberships">
                <Title level={3}>{activeMemberships}</Title>
              </Card>
            </Col>

            <Col xs={24} sm={12} lg={6}>
              <Card title="Outstanding Amount">
                <Title level={3}>{formatCurrency(outstandingAmount)}</Title>
              </Card>
            </Col>
          </Row>

          <Card title="Quick Actions" style={{ marginTop: 24 }}>
            <Space wrap>
              <Button type="primary" icon={<UserAddOutlined />} onClick={() => navigate('/members/create')}>
                Add Member
              </Button>

              <Button icon={<IdcardOutlined />} onClick={() => navigate('/memberships/create')}>
                Create Membership
              </Button>

              <Button icon={<DollarOutlined />}onClick={() => navigate('/payments/create')}>
                Record Payment
              </Button>
            </Space>
          </Card>

          <Card title="Recent Membership Activity" style={{ marginTop: 24 }}>
            {recentMembershipData.length > 0 ? (
              <Table
                columns={recentMembershipColumns}
                dataSource={recentMembershipData}
                pagination={false}
                rowClassName={(record) =>
                  record.expiringSoon ? 'membership-expiring-row' : ''
                }
              />
            ) : (
              <Empty
                description="No membership activity yet"
              />
            )}
          </Card>
        </>
      )}
    </div>
  );
}

export default DashboardPage;