import { Breadcrumb, Button, Card, DatePicker, Form, InputNumber, message, Modal, Select, Spin, Typography, } from 'antd';

import dayjs from 'dayjs';

import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { createMembership } from '../api/membershipApi';
import { getMembers } from '../api/memberApi';
import { getMembershipPricings } from '../api/membershipPricingApi';
import { MEMBERSHIP_TYPE_OPTIONS } from '../utils/membershipUtils';
import { useAuth } from '../context/AuthContext';
import { useBeforeUnload } from '../hooks/useBeforeUnload';

const MIN_MEMBERSHIP_AMOUNT = 500;
const MAX_START_DATE_DAYS_AHEAD = 30;

function CreateMembershipPage() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { role } = useAuth();

  const isAdmin = role === 'ADMIN';

  const [members, setMembers] = useState([]);
  const [membersLoading, setMembersLoading] = useState(true);
  const [membersError, setMembersError] = useState(null);

  const [pricings, setPricings] = useState([]);
  const [pricingsLoading, setPricingsLoading] = useState(true);
  const [pricingsError, setPricingsError] = useState(null);

  const [submitting, setSubmitting] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingValues, setPendingValues] = useState(null);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  const [formTouched, setFormTouched] = useState(false);
  const [amountTouched, setAmountTouched] = useState(false);

  const selectedMemberId = Form.useWatch('memberId', form);
  const selectedMembershipType = Form.useWatch('membershipType', form);
  const membershipAmount = Form.useWatch('membershipAmount', form);

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
    setPricingsLoading(true);
    setPricingsError(null);

    getMembershipPricings()
      .then((response) => {
        setPricings(response.data);
      })
      .catch(() => {
        setPricingsError('Failed to load membership pricing.');
      })
      .finally(() => {
        setPricingsLoading(false);
      });
  }, []);

  // Map of membershipType -> active price. Adjust field names here if the
  // actual pricing DTO differs (e.g. `amount` instead of `price`).
  const activePricingByType = useMemo(() => {
    return Object.fromEntries(
      pricings
        .filter((pricing) => pricing.active)
        .map((pricing) => [pricing.membershipType, pricing.price])
    );
  }, [pricings]);

  const activePriceForSelectedType = selectedMembershipType
    ? activePricingByType[selectedMembershipType]
    : undefined;

  const activeMembers = members.filter((member) => member.active);

  const memberOptions = activeMembers.map((member) => ({
    value: member.id,
    label: `${member.name} (${member.email})`,
  }));

  const selectedMember = activeMembers.find((member) => member.id === selectedMemberId);

  const today = dayjs().startOf('day');
  const maxStartDate = today.add(MAX_START_DATE_DAYS_AHEAD, 'day');
  const minStartDate = selectedMember?.joiningDate
    ? dayjs(selectedMember.joiningDate).startOf('day')
    : today;
  const effectiveMinStartDate = minStartDate.isAfter(today) ? minStartDate : today;

  const disabledStartDate = (current) => {
    if (!current) {
      return false;
    }

    return current.isBefore(effectiveMinStartDate, 'day') || current.isAfter(maxStartDate, 'day');
  };

  const handleMemberChange = () => {
    // Selected member's joining date may invalidate an already-picked start date.
    const currentStartDate = form.getFieldValue('startDate');

    if (currentStartDate && disabledStartDate(currentStartDate)) {
      form.setFieldsValue({ startDate: undefined });
    }
  };

  const handleMembershipTypeChange = () => {
    setAmountTouched(false);

    form.setFieldsValue({
      membershipAmount: undefined,
      paymentAmount: undefined,
    });
  };
  // Prefill amount fields from active pricing when the membership type changes,
  // but only if the admin hasn't manually overridden the amount already.
  useEffect(() => {
    if (!selectedMembershipType) {
      return;
    }

    if (activePriceForSelectedType === undefined) {
      if (!amountTouched) {
        form.setFieldsValue({
          membershipAmount: undefined,
          paymentAmount: undefined,
        });
      }

      return;
    }

    if (isAdmin) {
      if (!amountTouched) {
        form.setFieldsValue({
          membershipAmount: activePriceForSelectedType,
          paymentAmount: activePriceForSelectedType,
        });
      }
    } else {
      form.setFieldsValue({
        paymentAmount: activePriceForSelectedType,
      });
    }
  }, [
    selectedMembershipType,
    activePriceForSelectedType,
    isAdmin,
    amountTouched,
    form,
  ]);

  const handleMembershipAmountChange = (value) => {
    setAmountTouched(true);
    // Keep payment amount in sync with the membership price the admin sets,
    // while still letting them override it afterwards.
    form.setFieldsValue({ paymentAmount: value ?? undefined });
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
        membership: {
          memberId: pendingValues.memberId,
          membershipType: pendingValues.membershipType,
          startDate: pendingValues.startDate?.format('YYYY-MM-DD'),
          ...(isAdmin && pendingValues.membershipAmount !== undefined && pendingValues.membershipAmount !== null
            ? { amount: pendingValues.membershipAmount }
            : {}),
        },
        payment: {
          memberId: pendingValues.memberId,
          amount: pendingValues.paymentAmount,
          paymentDate: pendingValues.paymentDate?.format('YYYY-MM-DD'),
          paymentMode: pendingValues.paymentMode,
        },
      };

      await createMembership(payload);

      form.resetFields();
      setFormTouched(false);
      setAmountTouched(false);

      message.success('Membership created successfully');

      setConfirmOpen(false);
      setPendingValues(null);

      navigate('/memberships');
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to create membership');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (formTouched) {
      Modal.confirm({
        title: 'Discard changes?',
        content: 'You have unsaved membership details. Are you sure you want to leave?',
        okText: 'Discard',
        cancelText: 'Stay',
        onOk: () => navigate('/memberships'),
      });

      return;
    }

    navigate('/memberships');
  };

  useBeforeUnload(formTouched && !submitting);

  const confirmSelectedMember = activeMembers.find((member) => member.id === pendingValues?.memberId);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Breadcrumb
          items={[
            { title: 'Home', onClick: () => navigate('/dashboard') },
            { title: 'Memberships', onClick: () => navigate('/memberships') },
            { title: 'Add Membership' },
          ]}
        />

        <Button
          onClick={handleCancel}
          disabled={submitting || confirmOpen || resetConfirmOpen}
        >
          ← Back to Memberships
        </Button>
      </div>

      <div style={{ marginBottom: 24 }}>
        <Typography.Title level={2} style={{ marginBottom: 4 }}>
          Create Membership
        </Typography.Title>

        <Typography.Text type="secondary">
          Add a new membership and record the initial payment.
        </Typography.Text>
      </div>

      {membersError && (
        <Typography.Text type="danger" style={{ display: 'block', marginBottom: 8 }}>
          {membersError}
        </Typography.Text>
      )}

      {pricingsError && (
        <Typography.Text type="danger" style={{ display: 'block', marginBottom: 16 }}>
          {pricingsError}
        </Typography.Text>
      )}

      <Spin spinning={submitting} tip="Creating membership...">
        <Form form={form} layout="vertical" initialValues={{paymentMode: 'CASH',}} onFinish={handleSubmit} onValuesChange={() => setFormTouched(true)}>
          <Card title="Membership Information" size="small" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Member"
              name="memberId"
              rules={[{ required: true, message: 'Member is required' }]}
              tooltip="Only active members can have a new membership created."
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
                label="Membership Type"
                name="membershipType"
                rules={[
                  {
                    required: true,
                    message: 'Membership type is required',
                  },
                  {
                    validator: (_, value) => {
                      if (!value || isAdmin) {
                        return Promise.resolve();
                      }

                      const activePrice = activePricingByType[value];

                      if (activePrice === undefined) {
                        return Promise.reject(
                          new Error('No active pricing is available for this membership type')
                        );
                      }

                      return Promise.resolve();
                    },
                  },
                ]}
              >
              <Select
                placeholder="Select membership type"
                options={MEMBERSHIP_TYPE_OPTIONS}
                loading={pricingsLoading}
                onChange={handleMembershipTypeChange}
              />
            </Form.Item>

            <Form.Item
              label="Start Date"
              name="startDate"
              rules={[{ required: true, message: 'Start date is required' }]}
              tooltip={
                selectedMember
                  ? `Must be on or after the member's joining date (${selectedMember.joiningDate}) and within ${MAX_START_DATE_DAYS_AHEAD} days from today.`
                  : 'Select a member first to enable the start date.'
              }
            >
              <DatePicker
                style={{ width: '100%' }}
                disabled={!selectedMemberId}
                disabledDate={disabledStartDate}
                placeholder={selectedMemberId ? 'Select start date' : 'Select a member first'}
              />
            </Form.Item>

            {isAdmin ? (
              <Form.Item
                label="Membership Amount"
                name="membershipAmount"
                tooltip={`Prefilled from the active price for ${selectedMembershipType} membership type — override if needed.`}
                rules={[
                {
                  required: true,
                  message: 'Membership amount is required',
                },
                {
                  validator: (_, value) => {
                    if (value === undefined || value === null) {
                      return Promise.resolve();
                    }

                    if (value < MIN_MEMBERSHIP_AMOUNT) {
                      return Promise.reject(
                        new Error(
                          `Membership amount must be at least ${MIN_MEMBERSHIP_AMOUNT}`
                        )
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
                  placeholder="Enter amount to override standard price"
                  onChange={handleMembershipAmountChange}
                />
              </Form.Item>
            ) : (
              <Form.Item label="Membership Price" tooltip="Standard price for the selected membership type.">
                <InputNumber
                  style={{ width: '100%' }}
                  value={activePriceForSelectedType}
                  disabled
                  precision={2}
                  placeholder={selectedMembershipType ? 'No active pricing found' : 'Select a membership type'}
                />
              </Form.Item>
            )}
          </Card>

          <Card title="Payment Information" size="small" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Payment Amount"
              name="paymentAmount"
              rules={[
                { required: true, message: 'Payment amount is required' },
                {
                  validator: (_, value) => {
                    if (value === undefined || value === null) {
                      return Promise.resolve();
                    }

                    if (value < MIN_MEMBERSHIP_AMOUNT) {
                      return Promise.reject(new Error(`Payment amount must be at least ${MIN_MEMBERSHIP_AMOUNT}`));
                    }

                    const ceiling = isAdmin ? membershipAmount : activePriceForSelectedType;

                    if (ceiling !== undefined && ceiling !== null && value > ceiling) {
                      return Promise.reject(new Error('Payment amount cannot exceed the membership amount'));
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
                placeholder={`Enter payment amount (minimum ${MIN_MEMBERSHIP_AMOUNT})`}
              />
            </Form.Item>

            <Form.Item
              label="Payment Date"
              name="paymentDate"
              rules={[{ required: true, message: 'Payment date is required' }]}
            >
              <DatePicker
                style={{ width: '100%' }}
                disabledDate={(current) => current && current.isAfter(dayjs(), 'day')}
                placeholder="Select payment date"
              />
            </Form.Item>

            <Form.Item
              label="Payment Mode"
              name="paymentMode"
              rules={[{ required: true, message: 'Payment mode is required' }]}
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
                Create Membership
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Spin>

      <Modal
        title="Create Membership"
        open={confirmOpen}
        onCancel={() => {
          if (!submitting) {
            setConfirmOpen(false);
            setPendingValues(null);
          }
        }}
        onOk={handleConfirmSubmit}
        okText="Create Membership"
        confirmLoading={submitting}
        cancelText="Cancel"
      >
        <p>Are you sure you want to create this membership?</p>

        {pendingValues && (
          <div style={{ marginTop: 16 }}>
            <p><strong>Member:</strong> {confirmSelectedMember?.name || pendingValues.memberId}</p>
            <p>
              <strong>Membership Type:</strong>{' '}
              {MEMBERSHIP_TYPE_OPTIONS.find((option) => option.value === pendingValues.membershipType)?.label}
            </p>
            <p>
              <strong>Start Date:</strong>{' '}
              {pendingValues.startDate?.format('YYYY-MM-DD')}
            </p>
            {isAdmin && pendingValues.membershipAmount !== undefined && pendingValues.membershipAmount !== null && (
              <p><strong>Membership Amount:</strong> {pendingValues.membershipAmount}</p>
            )}
            <p><strong>Payment Amount:</strong> {pendingValues.paymentAmount}</p>
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
        <p>All entered membership and payment details will be cleared.</p>
      </Modal>
    </div>
  );
}

export default CreateMembershipPage;
