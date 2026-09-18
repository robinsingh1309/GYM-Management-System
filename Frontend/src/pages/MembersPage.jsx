import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { Alert, Button, Divider, Empty, Input, Modal, message, Select, Spin, Table, Tag } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';

import { getMembers, activateMember, deactivateMember } from '../api/memberApi';
import { useAuth } from '../context/AuthContext';

import { formatDate } from '../utils/dateUtils';

function MembersPage() {
  const navigate = useNavigate();
  const { role } = useAuth();

  const [members, setMembers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [actionLoading, setActionLoading] = useState(false);

  const [statusConfirmOpen, setStatusConfirmOpen] = useState(false);
  const [selectedMember, setSelectedMember] = useState(null);

  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const loadMembers = () => {
    setLoading(true);
    setError(null);

    getMembers()
      .then((response) => {
        setMembers(response.data);
      })
      .catch((error) => {
        setMembers(null);

        if (error.response?.status === 404) {
          setError(null);
        } else {
          setError('Failed to load members details.');
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadMembers();
  }, []);

  const handleMemberStatusChange = async () => {
    if (!selectedMember || actionLoading) {
      return;
    }

    setActionLoading(true);

    try {
      if (selectedMember.active) {
        await deactivateMember(selectedMember.id);
        message.success('Member deactivated successfully');
      } else {
        await activateMember(selectedMember.id);
        message.success('Member activated successfully');
      }

      setStatusConfirmOpen(false);
      setSelectedMember(null);

      await loadMembers();
    } catch (error) {
      message.error(
        error.response?.data?.message ||
          'Failed to update member status'
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusActionClick = (member) => {
    if (!member || actionLoading) {
      return;
    }

    setSelectedMember(member);
    setStatusConfirmOpen(true);
  };

  const filteredMembers = useMemo(() => members.filter((member) => {
    const searchValue = searchText.toLowerCase();

    const matchesSearch =
      member.name?.toLowerCase().includes(searchValue) ||
      member.email?.toLowerCase().includes(searchValue) ||
      member.phoneNumber?.toLowerCase().includes(searchValue);

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && member.active === true) ||
      (statusFilter === 'INACTIVE' && member.active === false);

    return matchesSearch && matchesStatus;
  }), [members, searchText, statusFilter]);

  const columns = useMemo(() => {
    const baseColumns = [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
      },
      {
        title: 'Name',
        dataIndex: 'name',
        key: 'name',
        render: (name, record) => (
          <Button type="link" style={{ padding: 0 }}
            onClick={() => navigate(`/members/${record.id}`)}
          >
            {name}
          </Button>
        ),
      },
      {
        title: 'Email',
        dataIndex: 'email',
        key: 'email',
      },
      {
        title: 'Phone',
        dataIndex: 'phoneNumber',
        key: 'phoneNumber',
      },
      {
        title: 'Joining Date',
        dataIndex: 'joiningDate',
        key: 'joiningDate',
        render: (value) => formatDate(value),
      },
      {
        title: 'Status',
        dataIndex: 'active',
        key: 'active',
        render: (active) => (
          <Tag color={active ? 'green' : 'red'}>
            {active ? 'Active' : 'Inactive'}
          </Tag>
        ),
      },
    ];

    if (role === 'ADMIN') {
      baseColumns.push({
        title: 'Action',
        key: 'action',
        render: (_, member) => (
          <Button danger={member.active}
            loading={actionLoading}
            onClick={() => handleStatusActionClick(member)}
          >
            {member.active ? 'Deactivate' : 'Activate'}
          </Button>
        ),
      });
    }

    return baseColumns;
  }, [navigate, role, actionLoading]);

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" message={error} showIcon/>;
  }

  return (
    <div>
      <div
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, padding: '0 0 16px', borderBottom: '1px solid #f0f0f0', }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>Members</h2>
          <div style={{ marginTop: 2, fontSize: 13, color: '#8c8c8c' }}>
            Manage all gym members
          </div>
          <div style={{ marginTop: 4, fontSize: 13, color: '#8c8c8c' }}>
            <strong>
              {filteredMembers.length}{' '}
              {filteredMembers.length === 1 ? 'Member' : 'Members'}
              {(searchText || statusFilter !== 'ALL') &&
                ` of ${members.length}`}
            </strong>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
          <span style={{ fontWeight: 500 }}>Filters:</span>

          <Input
            placeholder="Search by name, email or phone"
              prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
            style={{ width: 320 }}
          />

          <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 140 }}
            options={[
              { value: 'ALL', label: 'All Status' },
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' },
            ]}
          />

          <Button
            disabled={searchText === '' && statusFilter === 'ALL'}
            onClick={() => {
              setSearchText('');
              setStatusFilter('ALL');
            }}
          >
            Clear Filters
          </Button>
          <Divider orientation="vertical" />
          <Button icon={<ReloadOutlined />} onClick={loadMembers} loading={loading}>
            Refresh
          </Button>
          <Button type="primary" onClick={() => navigate('/members/create')}>
              Add Member
          </Button>
        </div>
      </div>

      {filteredMembers.length > 0 ? (
        <Table
          columns={columns}
          dataSource={filteredMembers}
          rowKey="id"
          scroll={{ x: 900 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} members`,
          }}
        />
      ) : (
          <Empty
            description={
              searchText || statusFilter !== 'ALL'
                ? 'No members match your search'
                : 'No members found'
            }
          />
      )}
      <Modal
        title={selectedMember?.active ? 'Deactivate Member' : 'Activate Member'}
        open={statusConfirmOpen}
        onCancel={() => {
          if (actionLoading) {
            return;
          }

          setStatusConfirmOpen(false);
          setSelectedMember(null);
        }}
        onOk={handleMemberStatusChange}
        okText={selectedMember?.active ? 'Deactivate' : 'Activate'}
        cancelText="Cancel"
        confirmLoading={actionLoading}
        okButtonProps={{
          danger: selectedMember?.active,
        }}
      >
        <p>
          Are you sure you want to{' '}
          {selectedMember?.active ? 'deactivate' : 'activate'}{' '}
          <strong>
            {selectedMember?.name}
          </strong>?
        </p>

        <p>
          {selectedMember?.active
            ? 'The member will be marked as inactive.'
            : 'The member will be marked as active.'}
        </p>
      </Modal>
    </div>
  );
}

export default MembersPage;