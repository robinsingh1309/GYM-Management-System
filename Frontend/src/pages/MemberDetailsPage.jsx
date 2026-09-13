import { Alert, Breadcrumb, Button, Card, Descriptions, Col, Empty, Row, Spin, Table, Tag } from 'antd';


import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { getMemberById } from '../api/memberApi';
import { getMembershipsByMemberId } from '../api/membershipApi';
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

  const membershipColumns = [
    {
      title: 'Membership',
      dataIndex: 'membershipType',
      key: 'membershipType',
      render: (type) => formatMembershipType(type),
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
          <div>Total Memberships</div>
          <h2>{membershipSummary.total}</h2>
        </Card>
      </Col>

      <Col xs={24} sm={12} md={8} lg={4}>
        <Card>
          <div>Active Memberships</div>
          <h2>{membershipSummary.active}</h2>
        </Card>
      </Col>

      <Col xs={24} sm={12} md={8} lg={4}>
        <Card>
          <div>Upcoming Memberships</div>
          <h2>{membershipSummary.upcoming}</h2>
        </Card>
      </Col>

      <Col xs={24} sm={12} md={8} lg={4}>
        <Card>
          <div>Expired Memberships</div>
          <h2>{membershipSummary.expired}</h2>
        </Card>
      </Col>

      <Col xs={24} sm={12} md={8} lg={8}>
        <Card>
          <div>Outstanding Amount</div>
          <h2>{formatCurrency(membershipSummary.outstandingAmount)}</h2>
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