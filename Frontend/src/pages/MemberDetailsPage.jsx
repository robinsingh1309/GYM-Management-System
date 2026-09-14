import { Alert, Breadcrumb, Button, Card, Descriptions, Col, Empty, Row, Space, Spin, Table, Tag } from 'antd';
import { CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined, DollarOutlined, TeamOutlined, } from '@ant-design/icons';

import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { getMemberById } from '../api/memberApi';
import { getMembershipsByMemberId } from '../api/membershipApi';
import { getMembershipById } from '../api/membershipApi';
import { formatPaymentStatus, formatMembershipStatus, formatMembershipType, calculateMembershipSummary } from '../utils/membershipUtils';
import { formatCurrency } from '../utils/currencyUtils';
import { formatDate } from '../utils/dateUtils';

function MemberDetailsPage() {
  const navigate = useNavigate();

  const { id } = useParams();

  const [member, setMember] = useState(null);
  const [memberships, setMemberships] = useState([]);
  const membershipSummary = calculateMembershipSummary(memberships);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);


  useEffect(() => {
    setLoading(true);
    setError(null)

    getMemberById(id)
      .then((response) => {
        setMember(response.data);

        return getMembershipsByMemberId(response.data.id);
      })
      .then((response) => {
        setMemberships(response.data);
      })
      .catch((error) => {
        setError('Failed to fetch member')
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

  if (!memberships) {
    return <Alert type="warning" title="Membership not found." />;
  }

  const membershipColumns = [
    {
      title: 'Membership Type',
      dataIndex: 'membershipType',
      key: 'membershipType',
      render: (value, record) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/memberships/${record.id}`)}>
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

        return <Tag color={colorMap[formatted] || 'default'}>{formatted}</Tag>;
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

        return <Tag color={colorMap[formatted] || 'default'}>{formatted}</Tag>;
      },
    },
  ];

  return (
    <div>
      <Breadcrumb items={[{title: 'Home',},{title: 'Members',},{title: 'Member Details',},]}/>
      <Button onClick={() => navigate('/members')}>
        ← Back to Members
      </Button>
      <div style={{ marginTop: 16 }}>
        <h1 style={{ marginBottom: 4 }}>
          {member ? member.name : 'Member Details'}
        </h1>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px' }}>
          <Spin size="large" />
        </div>
      ) : error ? (
        <Alert title="Unable to load member" description={error} type="error" showIcon/>
      ) : (
        member && (
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
                {member.dateOfBirth}
              </Descriptions.Item>

              <Descriptions.Item label="Gender">
                {member.gender}
              </Descriptions.Item>

              <Descriptions.Item label="Joining Date">
                {member.joiningDate}
              </Descriptions.Item>

              <Descriptions.Item label="Address">
                {member.address}
              </Descriptions.Item>

              <Descriptions.Item label="Status">
                <Tag color={member.active ? 'green' : 'red'}>
                  {member.active ? 'Active' : 'Inactive'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )
      )}

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card>
            <Space align="start">
              <TeamOutlined style={{ fontSize: 24 }} />
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
                style={{ fontSize: 24, color: '#52c41a' }}
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
                style={{ fontSize: 24, color: '#1677ff' }}
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
                style={{ fontSize: 24, color: '#ff4d4f' }}
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
                style={{ fontSize: 24, color: '#faad14' }}
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

      <Card title="Membership History" style={{ marginTop: 24 }}>
        {memberships.length > 0 ? (
          <Table
            columns={membershipColumns}
            dataSource={memberships}
            rowKey="id"
            pagination={false}
          />
        ) : (
          <Empty description="No membership history yet" />
        )}
      </Card>
    </div>
  );
}

export default MemberDetailsPage;