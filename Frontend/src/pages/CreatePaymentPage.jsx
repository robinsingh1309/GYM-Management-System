import { Alert, Breadcrumb, Button, Card, DatePicker, Form, InputNumber, message, Modal, Select, Spin, Typography, } from 'antd';

import dayjs from 'dayjs';

import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { createPayment } from '../api/paymentApi';
import { getMembers } from '../api/memberApi';
import { getMemberships } from '../api/membershipApi';

import { formatCurrency } from '../utils/currencyUtils';
import { formatMembershipType, formatMembershipStatus } from '../utils/membershipUtils';
import { useBeforeUnload } from '../hooks/useBeforeUnload';

function CreatePaymentPage() {
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const [members, setMembers] = useState([]);
  const [membersLoading, setMembersLoading] = useState(true);
  const [membersError, setMembersError] = useState(null);

  const [memberships, setMemberships] = useState([]);
  const [membershipsLoading, setMembershipsLoading] = useState(true);
  const [membershipsError, setMembershipsError] = useState(null);

  const [submitting, setSubmitting] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingValues, setPendingValues] = useState(null);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  const [formTouched, setFormTouched] = useState(false);
  const [amountTouched, setAmountTouched] = useState(false);

  const selectedMemberId = Form.useWatch('memberId', form);
  const selectedMembershipId = Form.useWatch('membershipId', form);

  useEffect(() => {
    setMembersLoading(true);
    setMembersError(null);

    getMembers()
      .then((response) => {
        setMembers(response.data);
      })
      .catch(() => {
        setMembersError('Failed to load members.');
      })
      .finally(() => {
        setMembersLoading(false);
      });
  }, []);

  useEffect(() => {
    setMembershipsLoading(true);
    setMembershipsError(null);

    getMemberships()
      .then((response) => {
        setMemberships(response.data);
      })
      .catch(() => {
        setMembershipsError('Failed to load memberships.');
      })
      .finally(() => {
        setMembershipsLoading(false);
      });
  }, []);

  const activeMembers = members.filter((member) => member.active);

  const memberOptions = activeMembers.map((member) => ({
    value: member.id,
    label: `${member.name} (${member.email})`,
  }));

  const payableMembershipsForMember = useMemo(() => {
    if (!selectedMemberId) {
      return [];
    }

    return memberships.filter(
      (membership) => membership.memberId === selectedMemberId && membership.outstandingAmount > 0
    );
  }, [memberships, selectedMemberId]);

  const membershipOptions = payableMembershipsForMember.map((membership) => ({
    value: membership.id,
    label: `${formatMembershipType(membership.membershipType)} — ${formatMembershipStatus(membership.status)} (Outstanding: ${formatCurrency(membership.outstandingAmount)})`,
  }));

  const selectedMembership = payableMembershipsForMember.find(
    (membership) => membership.id === selectedMembershipId
  );

  const disabledPaymentDate = (current) => current && current.isAfter(dayjs(), 'day');

  const isExpiredMembership = selectedMembership?.status === 'EXPIRED';

  const handleMemberChange = () => {
    setAmountTouched(false);

    form.setFieldsValue({
      membershipId: undefined,
      paymentAmount: undefined,
    });
  };

  const handleMembershipChange = () => {
    setAmountTouched(false);
  };

  useEffect(() => {
    if (!selectedMembership) {
      return;
    }

    if (isExpiredMembership || !amountTouched) {
      form.setFieldsValue({ paymentAmount: selectedMembership.outstandingAmount });
    }
  }, [selectedMembership, isExpiredMembership, amountTouched, form]);

  const handlePaymentAmountChange = () => {
    setAmountTouched(true);
  };

  const handleSubmit = (values) => {
    setPendingValues(values);
    setConfirmOpen(true);
  };

  const handleConfirmSubmit = async () => {
    if (!pendingValues || submitting) {
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        memberId: pendingValues.memberId,
        membershipId: pendingValues.membershipId,
        amount: pendingValues.paymentAmount,
        paymentDate: pendingValues.paymentDate?.format('YYYY-MM-DD'),
        paymentMode: pendingValues.paymentMode,
      };

      await createPayment(payload);

      form.resetFields();
      setFormTouched(false);
      setAmountTouched(false);

      message.success('Payment recorded successfully');

      setConfirmOpen(false);
      setPendingValues(null);

      navigate('/payments');
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to record payment');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (formTouched) {
      Modal.confirm({
        title: 'Discard changes?',
        content: 'You have unsaved payment details. Are you sure you want to leave?',
        okText: 'Discard',
        cancelText: 'Stay',
        onOk: () => navigate('/payments'),
      });

      return;
    }

    navigate('/payments');
  };

  useBeforeUnload(formTouched && !submitting);

  const confirmSelectedMember = activeMembers.find((member) => member.id === pendingValues?.memberId);
  const confirmSelectedMembership = payableMembershipsForMember.find(
    (membership) => membership.id === pendingValues?.membershipId
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Breadcrumb
          items={[
            { title: 'Home', onClick: () => navigate('/dashboard') },
            { title: 'Payments', onClick: () => navigate('/payments') },
            { title: 'Add Payment' },
          ]}
        />

        <Button
          onClick={handleCancel}
          disabled={submitting || confirmOpen || resetConfirmOpen}
        >
          ← Back to Payments
        </Button>
      </div>

      <div style={{ marginBottom: 24 }}>
        <Typography.Title level={2} style={{ marginBottom: 4 }}>
          Record Payment
        </Typography.Title>

        <Typography.Text type="secondary">
          Record a payment against an existing membership.
        </Typography.Text>
      </div>

      {membersError && (
        <Typography.Text type="danger" style={{ display: 'block', marginBottom: 8 }}>
          {membersError}
        </Typography.Text>
      )}

      {membershipsError && (
        <Typography.Text type="danger" style={{ display: 'block', marginBottom: 16 }}>
          {membershipsError}
        </Typography.Text>
      )}

      <Spin spinning={submitting} tip="Recording payment...">
        <Form form={form} layout="vertical" initialValues={{ paymentMode: 'CASH' }} onFinish={handleSubmit} onValuesChange={() => setFormTouched(true)}>
          <Card title="Payment Details" size="small" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Member"
              name="memberId"
              rules={[{ required: true, message: 'Member is required' }]}
            >
              <Select
                placeholder="Select member"
                showSearch
                loading={membersLoading}
                options={memberOptions}
                optionFilterProp="label"
                filterOption={(input, option) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
                }
                onChange={handleMemberChange}
              />
            </Form.Item>

            <Form.Item
              label="Membership"
              name="membershipId"
              rules={[{ required: true, message: 'Membership is required' }]}
              tooltip="Only memberships with an outstanding balance are shown."
            >
              <Select
                placeholder={selectedMemberId ? 'Select membership' : 'Select a member first'}
                disabled={!selectedMemberId}
                loading={membershipsLoading}
                options={membershipOptions}
                notFoundContent={
                  selectedMemberId ? 'No memberships with an outstanding balance for this member' : undefined
                }
                onChange={handleMembershipChange}
              />
            </Form.Item>

            {isExpiredMembership && (
              <Alert
                type="warning"
                showIcon
                message="This membership has expired. The full outstanding balance must be paid in a single payment."
                style={{ marginBottom: 16 }}
              />
            )}

            <Form.Item
              label="Payment Amount"
              name="paymentAmount"
              tooltip={
                selectedMembership
                  ? `Outstanding balance: ${formatCurrency(selectedMembership.outstandingAmount)}`
                  : 'Select a membership first.'
              }
              rules={[
                { required: true, message: 'Payment amount is required' },
                {
                  validator: (_, value) => {
                    if (value === undefined || value === null || !selectedMembership) {
                      return Promise.resolve();
                    }

                    if (value <= 0) {
                      return Promise.reject(new Error('Payment amount must be greater than 0'));
                    }

                    if (value > selectedMembership.outstandingAmount) {
                      return Promise.reject(new Error('Payment amount cannot exceed the outstanding balance'));
                    }

                    if (isExpiredMembership && value !== selectedMembership.outstandingAmount) {
                      return Promise.reject(
                        new Error('For an expired membership, payment must equal the full outstanding balance')
                      );
                    }

                    return Promise.resolve();
                  },
                },
              ]}
            >
              <InputNumber
                style={{ width: '100%' }}
                min={0}
                precision={2}
                disabled={!selectedMembership || isExpiredMembership}
                placeholder={selectedMembership ? 'Enter payment amount' : 'Select a membership first'}
                onChange={handlePaymentAmountChange}
              />
            </Form.Item>

            <Form.Item
              label="Payment Date"
              name="paymentDate"
              rules={[{ required: true, message: 'Payment date is required' }]}
            >
              <DatePicker
                style={{ width: '100%' }}
                disabledDate={disabledPaymentDate}
                placeholder="Select payment date"
              />
            </Form.Item>

            <Form.Item label="Payment Mode" name="paymentMode"
                rules={[
                    {
                    required: true,
                    message: 'Payment mode is required',
                    },
                ]}
                >
                <Select options={[
                    {
                        value: 'CASH',
                        label: 'Cash',
                    },
                ]}
                    disabled
                />
            </Form.Item>
          </Card>

          <Form.Item>
            <div style={{ display: 'flex', gap: 12 }}>
              <Button onClick={() => setResetConfirmOpen(true)} disabled={submitting || confirmOpen || !formTouched}>
                Reset
              </Button>

              <Button onClick={handleCancel} disabled={submitting || confirmOpen}>
                Cancel
              </Button>

              <Button type="primary" htmlType="submit" loading={submitting}>
                Record Payment
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Spin>

      <Modal
        title="Record Payment"
        open={confirmOpen}
        onCancel={() => {
          if (!submitting) {
            setConfirmOpen(false);
            setPendingValues(null);
          }
        }}
        onOk={handleConfirmSubmit}
        okText="Record Payment"
        confirmLoading={submitting}
        cancelText="Cancel"
      >
        <p>Are you sure you want to record this payment?</p>

        {pendingValues && (
          <div style={{ marginTop: 16 }}>
            <p><strong>Member:</strong> {confirmSelectedMember?.name || pendingValues.memberId}</p>
            <p>
              <strong>Membership:</strong>{' '}
              {confirmSelectedMembership
                ? formatMembershipType(confirmSelectedMembership.membershipType)
                : pendingValues.membershipId}
            </p>
            <p><strong>Payment Amount:</strong> {formatCurrency(pendingValues.paymentAmount)}</p>
            <p>
              <strong>Payment Date:</strong>{' '}
              {pendingValues.paymentDate?.format('YYYY-MM-DD')}
            </p>
            <p>
                <strong>Payment Mode:</strong> Cash
            </p>
          </div>
        )}
      </Modal>

      <Modal
        title="Reset Form"
        open={resetConfirmOpen}
        onCancel={() => setResetConfirmOpen(false)}
        onOk={() => {
          form.resetFields();
          setFormTouched(false);
          setAmountTouched(false);
          setResetConfirmOpen(false);
        }}
        okText="Reset"
        cancelText="Cancel"
      >
        <p>Are you sure you want to reset the form?</p>
        <p>All entered payment details will be cleared.</p>
      </Modal>
    </div>
  );
}

export default CreatePaymentPage;
