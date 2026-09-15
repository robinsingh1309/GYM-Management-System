import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { Alert, Button, Empty, Input, Select, Spin, Table, Tag } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';

import { getMembers } from '../api/memberApi';

import { formatDate } from '../utils/dateUtils';

function MembersPage() {
  const navigate = useNavigate();

  const [members, setMembers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
        setError('Failed to load members.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadMembers();
  }, []);

  const filteredMembers = members.filter((member) => {
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
  });

  const columns = [
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
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Button type="link"
          onClick={() => navigate(`/members/${record.id}`)}
        >
          View
        </Button>
      ),
    },
  ];

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" title={error} />;
  }

  return (
    <div>
      <div
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, padding: '0 0 16px', marginBottom: 16, borderBottom: '1px solid #f0f0f0', }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>Members</h2>
          <div style={{ marginTop: 2, fontSize: 13, color: '#8c8c8c' }}>
            Manage all gym members
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
          <Input
            placeholder="Search by name, email or phone"
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
            allowClear
            style={{ width: 260 }}
          />
          <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 130 }}
            options={[
              { value: 'ALL', label: 'All status' },
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' },
            ]}
          />
          <div style={{ width: 1, height: 20, background: '#f0f0f0', margin: '0 4px' }} />
          <Button icon={<ReloadOutlined />} onClick={loadMembers}>
            Refresh
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
    </div>
  );
}

export default MembersPage;