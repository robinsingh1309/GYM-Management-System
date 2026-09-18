import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { Alert, Button, Divider, Empty, Input, Modal, message, Select, Spin, Table, Tag } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';

import { getMembershipPricings, activateMembershipPricing, deactivateMembershipPricing } from '../api/membershipPricingApi';
import { useAuth } from '../context/AuthContext';

import { formatDate } from '../utils/dateUtils';
import { formatCurrency } from '../utils/currencyUtils';
import { formatMembershipType, MEMBERSHIP_TYPE_OPTIONS } from '../utils/membershipUtils';

function MembershipsTypePage() {
  const navigate = useNavigate();
  const { role } = useAuth();

  const [pricings, setPricings] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [actionLoading, setActionLoading] = useState(false);
  const [statusConfirmOpen, setStatusConfirmOpen] = useState(false);
  const [selectedPricing, setSelectedPricing] = useState(null);

  const [searchText, setSearchText] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [activeFilter, setActiveFilter] = useState('ALL');

  const loadPricings = () => {
    setLoading(true);
    setError(null);

    getMembershipPricings()
      .then((response) => {
        setPricings(response.data);
      })
      .catch(() => {
        setError('Failed to load membership types.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadPricings();
  }, []);

  const getPricingAction = (pricing) =>
    pricing.active
      ? { type: 'deactivate', label: 'Deactivate' }
      : { type: 'activate', label: 'Activate' };

  const handlePricingStatusChange = async () => {
    if (!selectedPricing || actionLoading) {
      return;
    }

    const action = getPricingAction(selectedPricing);

    setActionLoading(true);

    try {
      if (action.type === 'activate') {
        await activateMembershipPricing(selectedPricing.id);
        message.success('Membership type activated successfully');
      } else {
        await deactivateMembershipPricing(selectedPricing.id);
        message.success('Membership type deactivated successfully');
      }

      setStatusConfirmOpen(false);
      setSelectedPricing(null);

      await loadPricings();
    } catch (error) {
      message.error(
        error.response?.data?.message ||
          'Failed to update membership type status'
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusActionClick = (pricing) => {
    if (!pricing || actionLoading) {
      return;
    }

    setSelectedPricing(pricing);
    setStatusConfirmOpen(true);
  };

  const filteredPricings = useMemo(() => pricings.filter((pricing) => {
    const searchValue = searchText.toLowerCase();

    const matchesSearch =
      searchValue === '' ||
      formatMembershipType(pricing.membershipType)?.toLowerCase().includes(searchValue);

    const matchesType =
      typeFilter === 'ALL' || pricing.membershipType === typeFilter;

    const matchesActive =
      activeFilter === 'ALL' ||
      (activeFilter === 'ACTIVE' && pricing.active) ||
      (activeFilter === 'INACTIVE' && !pricing.active);

    return matchesSearch && matchesType && matchesActive;
  }), [pricings, searchText, typeFilter, activeFilter]);

  const columns = useMemo(() => {
    const baseColumns = [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
      },
      {
        title: 'Membership Type',
        dataIndex: 'membershipType',
        key: 'membershipType',
        render: (value) => (
          <Tag>{formatMembershipType(value)}</Tag>
        ),
      },
      {
        title: 'Price',
        dataIndex: 'price',
        key: 'price',
        render: (value) => formatCurrency(value),
      },
      {
        title: 'Status',
        dataIndex: 'active',
        key: 'active',
        render: (active) => (
          <Tag color={active ? 'green' : 'default'}>
            {active ? 'Active' : 'Inactive'}
          </Tag>
        ),
      },
      {
        title: 'Created On',
        dataIndex: 'createdAt',
        key: 'createdAt',
        render: (value) => formatDate(value),
      },
      {
        title: 'Updated On',
        dataIndex: 'updatedAt',
        key: 'updatedAt',
        render: (value) => formatDate(value),
      },
    ];

    if (role === 'ADMIN') {
      baseColumns.push({
        title: 'Action',
        key: 'action',
        render: (_, pricing) => {
          const action = getPricingAction(pricing);

          return (
            <Button
              danger={action.type === 'deactivate'}
              loading={actionLoading}
              onClick={() => handleStatusActionClick(pricing)}
            >
              {action.label}
            </Button>
          );
        },
      });
    }

    return baseColumns;
  }, [role, actionLoading]);

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" message={error} showIcon/>;
  }

  const confirmAction = selectedPricing ? getPricingAction(selectedPricing) : { type: null };

  return (
    <div>
      <div
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, padding: '0 0 16px', borderBottom: '1px solid #f0f0f0', }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>Membership Types</h2>
          <div style={{ marginTop: 2, fontSize: 13, color: '#8c8c8c' }}>
            Manage membership pricing
          </div>
          <div style={{ marginTop: 4, fontSize: 13, color: '#8c8c8c' }}>
            <strong>
              {filteredPricings.length}{' '}
              {filteredPricings.length === 1 ? 'Membership Type' : 'Membership Types'}
              {(searchText || typeFilter !== 'ALL' || activeFilter !== 'ALL') &&
                ` of ${pricings.length}`}
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
            style={{ width: 260 }}
          />

          <Select value={typeFilter} onChange={setTypeFilter} style={{ width: 180 }}
            options={[
              { value: 'ALL', label: 'All Membership Types' },
              ...MEMBERSHIP_TYPE_OPTIONS,
            ]}
          />

          <Select value={activeFilter} onChange={setActiveFilter} style={{ width: 150 }}
            options={[
              { value: 'ALL', label: 'All Statuses' },
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' },
            ]}
          />

          <Button
            disabled={searchText === '' && typeFilter === 'ALL' && activeFilter === 'ALL'}
            onClick={() => {
              setSearchText('');
              setTypeFilter('ALL');
              setActiveFilter('ALL');
            }}
          >
            Clear Filters
          </Button>
          <Divider orientation="vertical" />
          <Button icon={<ReloadOutlined />} onClick={loadPricings} loading={loading}>
            Refresh
          </Button>
          {role === 'ADMIN' && (
            <Button type="primary" onClick={() => navigate('/membership-types/create')}>
              Add Membership Type
            </Button>
          )}
        </div>
      </div>

      {filteredPricings.length > 0 ? (
        <Table
          columns={columns}
          dataSource={filteredPricings}
          rowKey="id"
          scroll={{ x: 900 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} membership types`,
          }}
        />
      ) : (
        <Empty
          description={
            searchText || typeFilter !== 'ALL' || activeFilter !== 'ALL'
              ? 'No membership types match your search'
              : 'No membership types found'
          }
        />
      )}

      <Modal
        title={confirmAction.type === 'deactivate' ? 'Deactivate Membership Type' : 'Activate Membership Type'}
        open={statusConfirmOpen}
        onCancel={() => {
          if (actionLoading) {
            return;
          }

          setStatusConfirmOpen(false);
          setSelectedPricing(null);
        }}
        onOk={handlePricingStatusChange}
        okText={confirmAction.type === 'deactivate' ? 'Deactivate' : 'Activate'}
        cancelText="Cancel"
        confirmLoading={actionLoading}
        okButtonProps={{
          danger: confirmAction.type === 'deactivate',
        }}
      >
        <p>
          Are you sure you want to{' '}
          {confirmAction.type === 'deactivate' ? 'deactivate' : 'activate'}{' '}
          <strong>{selectedPricing ? formatMembershipType(selectedPricing.membershipType) : ''}</strong>{' '}
          pricing at <strong>{selectedPricing ? formatCurrency(selectedPricing.price) : ''}</strong>?
        </p>

        {confirmAction.type === 'activate' && (
          <p>
            Activating this will automatically deactivate the currently active price for this
            membership type, if any.
          </p>
        )}

        {confirmAction.type === 'deactivate' && (
          <p>
            Members purchasing this membership type will no longer see this price as an option
            until it, or another price for this type, is reactivated.
          </p>
        )}
      </Modal>
    </div>
  );
}

export default MembershipsTypePage;