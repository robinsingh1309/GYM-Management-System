import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { Alert, Button, Divider, Empty, Input, Modal, message, Select, Spin, Table, Tag, Tooltip } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';

import { getMemberships, activateMembership, deactivateMembership } from '../api/membershipApi';
import { useAuth } from '../../../context/AuthContext';

import { formatDate } from '../../../utils/dateUtils';
import { formatCurrency } from '../../../utils/currencyUtils';
import { formatMembershipType, formatMembershipStatus, formatPaymentStatus } from '../../../utils/membershipUtils';

function MembershipsPage() {
  const navigate = useNavigate();
  const { role } = useAuth();

  const [memberships, setMemberships] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [actionLoading, setActionLoading] = useState(false);
  const [statusConfirmOpen, setStatusConfirmOpen] = useState(false);
  const [selectedMembership, setSelectedMembership] = useState(null);

  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [paymentStatusFilter, setPaymentStatusFilter] = useState('ALL');

  const loadMemberships = () => {
    setLoading(true);
    setError(null);

    getMemberships()
      .then((response) => {
        setMemberships(response.data);
      })
      .catch(() => {
        setError('Failed to load memberships.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadMemberships();
  }, []);

  const getMembershipAction = (membership) => {
    if (membership.status === 'INACTIVE') {
      return { type: 'activate', label: 'Activate' };
    }

    if (membership.status === 'UPCOMING' || membership.status === 'ACTIVE') {
      return { type: 'deactivate', label: 'Deactivate' };
    }

    return { type: null, reason: 'Expired memberships cannot be activated or deactivated.' };
  };

  const handleMembershipStatusChange = async () => {
    if (!selectedMembership || actionLoading) {
      return;
    }

    const action = getMembershipAction(selectedMembership);

    if (!action.type) {
      return;
    }

    setActionLoading(true);

    try {
      if (action.type === 'activate') {
        await activateMembership(selectedMembership.id);
        message.success('Membership activated successfully');
      } else {
        await deactivateMembership(selectedMembership.id);
        message.success('Membership deactivated successfully');
      }

      setStatusConfirmOpen(false);
      setSelectedMembership(null);

      await loadMemberships();
    } catch (error) {
      message.error(
        error.response?.data?.message ||
          'Failed to update membership status'
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusActionClick = (membership) => {
    if (!membership || actionLoading) {
      return;
    }

    setSelectedMembership(membership);
    setStatusConfirmOpen(true);
  };

  const filteredMemberships = useMemo(() => memberships.filter((membership) => {
    const searchValue = searchText.toLowerCase();

    const matchesSearch =
      formatMembershipType(membership.membershipType)?.toLowerCase().includes(searchValue);

    const matchesStatus =
      statusFilter === 'ALL' || membership.status === statusFilter;

    const matchesPaymentStatus =
      paymentStatusFilter === 'ALL' || membership.paymentStatus === paymentStatusFilter;

    return matchesSearch && matchesStatus && matchesPaymentStatus;
  }), [memberships, searchText, statusFilter, paymentStatusFilter]);

  const columns = useMemo(() => {
    const baseColumns = [
      {
        title: 'Membership ID',
        dataIndex: 'id',
        key: 'id',
        render: (id) => (
          <Button type="link"
            style={{ padding: 0 }}
            onClick={() => navigate(`/memberships/${id}`)}
          >
            {id}
          </Button>
        ),
      },
      {
        title: 'Member ID',
        dataIndex: 'memberId',
        key: 'memberId',
      },
      {
        title: 'Membership Type',
        dataIndex: 'membershipType',
        key: 'membershipType',
        render: (value) => (
          <Tag>{formatMembershipType(value)}</Tag>),
      },
      {
        title: 'Start Date',
        dataIndex: 'startDate',
        key: 'startDate',
        render: (value) => formatDate(value),
      },
      {
        title: 'End Date',
        dataIndex: 'endDate',
        key: 'endDate',
        render: (value) => formatDate(value),
      },
      {
        title: 'Amount',
        dataIndex: 'amount',
        key: 'amount',
        render: (value) => formatCurrency(value),
      },
      {
        title: 'Paid',
        dataIndex: 'totalPaid',
        key: 'totalPaid',
        render: (value) => formatCurrency(value),
      },
      {
        title: 'Outstanding',
        dataIndex: 'outstandingAmount',
        key: 'outstandingAmount',
        render: (value) => formatCurrency(value),
      },
      {
        title: 'Payment Status',
        dataIndex: 'paymentStatus',
        key: 'paymentStatus',
        render: (value) => (
          <Tag
            color={
              value === 'PAID'
                ? 'green'
                : value === 'PARTIALLY_PAID'
                  ? 'orange'
                  : 'red'
            }
          >
            {formatPaymentStatus(value)}
          </Tag>
        ),
      },
      {
        title: 'Membership Status',
        dataIndex: 'status',
        key: 'status',
        render: (value) => (
          <Tag
            color={
              value === 'ACTIVE'
                ? 'green'
                : value === 'UPCOMING'
                  ? 'blue'
                  : value === 'EXPIRED'
                    ? 'red'
                    : 'default'
            }
          >
            {formatMembershipStatus(value)}
          </Tag>
        ),
      },
      {
        title: 'Active',
        dataIndex: 'active',
        key: 'active',
        render: (active) => (
          <Tag color={active ? 'green' : 'default'}>
            {active ? 'Active' : 'Inactive'}
          </Tag>
        ),
      },
    ];

    if (role === 'ADMIN') {
      baseColumns.push({
        title: 'Action',
        key: 'action',
        render: (_, membership) => {
          const action = getMembershipAction(membership);

          if (!action.type) {
            return (
              <Tooltip title={action.reason}>
                <Tag>No action</Tag>
              </Tooltip>
            );
          }

          return (
            <Button
              className={action.type === 'deactivate' ? 'fm-deactivate-button' : 'fm-activate-button'}
              loading={actionLoading}
              onClick={() => handleStatusActionClick(membership)}
            >
              {action.label}
            </Button>
          );
        },
      });
    }

    return baseColumns;
  }, [navigate, role, actionLoading]);

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" message={error} />;
  }

  const confirmAction = selectedMembership ? getMembershipAction(selectedMembership) : { type: null };

  return (
    <div>
      <div
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, padding: '0 0 16px', borderBottom: '1px solid #f0f0f0', }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>Memberships</h2>
          <div style={{ marginTop: 2, fontSize: 13, color: '#8c8c8c' }}>
            Manage all memberships
          </div>
          <div style={{ marginTop: 4, fontSize: 13, color: '#8c8c8c' }}>
            <strong>
              {filteredMemberships.length}{' '}
              {filteredMemberships.length === 1 ? 'Membership' : 'Memberships'}
              {(searchText || statusFilter !== 'ALL' || paymentStatusFilter !== 'ALL') &&
                ` of ${memberships.length}`}
            </strong>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
          <span style={{ fontWeight: 500 }}>Filters:</span>

          <Input
            placeholder="Search by Membership Type"
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />

          <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 165 }}
            options={[
              { value: 'ALL', label: 'Membership Status' },
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' },
              { value: 'UPCOMING', label: 'Upcoming' },
              { value: 'EXPIRED', label: 'Expired' },
            ]}
          />

          <Select value={paymentStatusFilter} onChange={setPaymentStatusFilter} style={{ width: 165 }}
            options={[
              { value: 'ALL', label: 'All Payment Status' },
              { value: 'PAID', label: 'Paid' },
              { value: 'PARTIALLY_PAID', label: 'Partially Paid' },
              { value: 'UNPAID', label: 'Unpaid' },
            ]}
          />

          <Button
            className="fm-secondary-button"
            disabled={searchText === '' && statusFilter === 'ALL' && paymentStatusFilter === 'ALL'}
            onClick={() => {
              setSearchText('');
              setStatusFilter('ALL');
              setPaymentStatusFilter('ALL');
            }}
          >
            Clear Filters
          </Button>
          <Divider orientation="vertical" />
          <Button className="fm-refresh-button" icon={<ReloadOutlined />} onClick={loadMemberships} loading={loading}>
            Refresh
          </Button>
          <Button type="primary" onClick={() => navigate('/memberships/create')}>
            Add Membership
          </Button>
        </div>
      </div>

      {filteredMemberships.length > 0 ? (
        <Table
          columns={columns}
          dataSource={filteredMemberships}
          rowKey="id"
          scroll={{ x: 1200 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} memberships`,
          }}
        />
      ) : (
        <Empty
          description={
            searchText || statusFilter !== 'ALL' || paymentStatusFilter !== 'ALL'
              ? 'No memberships match your search'
              : 'No memberships found'
          }
        />
      )}

      <Modal
        title={confirmAction.type === 'deactivate' ? 'Deactivate Membership' : 'Activate Membership'}
        open={statusConfirmOpen}
        onCancel={() => {
          if (actionLoading) {
            return;
          }

          setStatusConfirmOpen(false);
          setSelectedMembership(null);
        }}
        onOk={handleMembershipStatusChange}
        okText={confirmAction.type === 'deactivate' ? 'Deactivate' : 'Activate'}
        cancelText="Cancel"
        confirmLoading={actionLoading}
        okButtonProps={{
          danger: confirmAction.type === 'deactivate',
        }}
      >
        <p>
          Are you sure you want to{' '}
          {confirmAction.type === 'deactivate' ? 'deactivate' : 'activate'} membership{' '}
          <strong>#{selectedMembership?.id}</strong>?
        </p>

        <p>
          {confirmAction.type === 'deactivate'
            ? 'The membership will be marked as inactive.'
            : 'The membership will be marked as active.'}
        </p>
      </Modal>
    </div>
  );
}

export default MembershipsPage;