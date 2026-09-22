import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { Alert, Button, DatePicker, Divider, Empty, Input, Spin, Table, Tag } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter';
import isSameOrBefore from 'dayjs/plugin/isSameOrBefore';

dayjs.extend(isSameOrAfter);
dayjs.extend(isSameOrBefore);

import { getPayments } from '../api/paymentApi';

import { formatDate } from '../../../utils/dateUtils';
import { formatCurrency } from '../../../utils/currencyUtils';
import { formatPaymentMode, getPaymentModeColor } from '../../../utils/paymentUtils';

const { RangePicker } = DatePicker;

function PaymentsPage() {
  const navigate = useNavigate();

  const [payments, setPayments] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchText, setSearchText] = useState('');
  const [dateRange, setDateRange] = useState(null); // [dayjs, dayjs] | null

  const loadPayments = () => {
    setLoading(true);
    setError(null);

    getPayments()
      .then((response) => {
        setPayments(response.data);
      })
      .catch(() => {
        setError('Failed to load payments.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    loadPayments();
  }, []);

  const filteredPayments = useMemo(() => payments.filter((payment) => {
    const searchValue = searchText.trim().toLowerCase();

    const matchesSearch =
      searchValue === '' ||
      String(payment.memberId).toLowerCase().includes(searchValue) ||
      String(payment.membershipId).toLowerCase().includes(searchValue);

    const matchesPaymentMode = true;

    const matchesDateRange =
      !dateRange ||
      !dateRange[0] ||
      !dateRange[1] ||
      (
        dayjs(payment.paymentDate).isSameOrAfter(dateRange[0], 'day') &&
        dayjs(payment.paymentDate).isSameOrBefore(dateRange[1], 'day')
      );

    return matchesSearch && matchesPaymentMode && matchesDateRange;
  }), [payments, searchText, dateRange]);

  const columns = useMemo(() => [
    {
      title: 'Payment ID',
      dataIndex: 'id',
      key: 'id',
      render: (id) => (
        <Button type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/payments/${id}`)}
        >
          {id}
        </Button>
      ),
    },
    {
      title: 'Member ID',
      dataIndex: 'memberId',
      key: 'memberId',
      render: (memberId) => (
        <Button type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/members/${memberId}`)}
        >
          {memberId}
        </Button>
      ),
    },
    {
      title: 'Membership ID',
      dataIndex: 'membershipId',
      key: 'membershipId',
      render: (membershipId) => (
        <Button type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/memberships/${membershipId}`)}
        >
          {membershipId}
        </Button>
      ),
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (value) => formatCurrency(value),
    },
    {
      title: 'Payment Date',
      dataIndex: 'paymentDate',
      key: 'paymentDate',
      render: (value) => formatDate(value),
    },
    {
      title: 'Payment Mode',
      dataIndex: 'paymentMode',
      key: 'paymentMode',
      render: (value) => (
        <Tag color={getPaymentModeColor(value)}>
          {formatPaymentMode(value)}
        </Tag>
      ),
    },
  ], [navigate]);

  if (loading) {
    return <Spin size="large" />;
  }

  if (error) {
    return <Alert type="error" message={error} />;
  }

  return (
    <div>
      <div
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, padding: '0 0 16px', borderBottom: '1px solid #f0f0f0', }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>Payments</h2>
          <div style={{ marginTop: 2, fontSize: 13, color: '#8c8c8c' }}>
            View all recorded payments
          </div>
          <div style={{ marginTop: 4, fontSize: 13, color: '#8c8c8c' }}>
            <strong>
              {filteredPayments.length}{' '}
              {filteredPayments.length === 1 ? 'Payment' : 'Payments'}
              {(searchText || dateRange) &&
                ` of ${payments.length}`}
            </strong>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
          <span style={{ fontWeight: 500 }}>Filters:</span>

          <Input
            placeholder="Search by Member or Membership ID"
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
            style={{ width: 300 }}
          />

          <RangePicker
            value={dateRange}
            onChange={(range) => setDateRange(range)}
            allowClear
          />

          <Button
            className="fm-secondary-button"
            disabled={searchText === '' && !dateRange}
            onClick={() => {
              setSearchText('');
              setDateRange(null);
            }}
          >
            Clear Filters
          </Button>
          <Divider orientation="vertical" />
          <Button className="fm-refresh-button" icon={<ReloadOutlined />} onClick={loadPayments} loading={loading}>
            Refresh
          </Button>
          <Button type="primary" onClick={() => navigate('/payments/create')}>
            Add Payment
          </Button>
        </div>
      </div>

      {filteredPayments.length > 0 ? (
        <Table
          columns={columns}
          dataSource={filteredPayments}
          rowKey="id"
          scroll={{ x: 900 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} payments`,
          }}
        />
      ) : (
        <Empty
          description={
            searchText || dateRange
              ? 'No payments match your search'
              : 'No payments found'
          }
        />
      )}
    </div>
  );
}

export default PaymentsPage;