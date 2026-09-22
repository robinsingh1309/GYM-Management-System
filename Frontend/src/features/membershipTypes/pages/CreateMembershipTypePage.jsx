import { Alert, Breadcrumb, Button, Card, Form, InputNumber, message, Modal, Select, Spin, Typography, } from 'antd';

import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

import { createMembershipPricing, getMembershipPricings } from '../api/membershipPricingApi';

import { formatCurrency } from '../../../utils/currencyUtils';
import { formatMembershipType, MEMBERSHIP_TYPE_OPTIONS } from '../../../utils/membershipUtils';
import { useAuth } from '../../../context/AuthContext';
import { useBeforeUnload } from '../../../hooks/useBeforeUnload';

const MINIMUM_PRICE = 500;

function CreateMembershipTypePage() {
  const { role } = useAuth();

  const isAdmin = role === 'ADMIN';
  
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const [pricings, setPricings] = useState([]);
  const [pricingsLoading, setPricingsLoading] = useState(true);
  const [pricingsError, setPricingsError] = useState(null);

  const [submitting, setSubmitting] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingValues, setPendingValues] = useState(null);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  const [formTouched, setFormTouched] = useState(false);

  const selectedMembershipType = Form.useWatch('membershipType', form);
  const price = Form.useWatch('price', form);

  useEffect(() => {
    setPricingsLoading(true);
    setPricingsError(null);

    getMembershipPricings()
      .then((response) => {
        setPricings(response.data);
      })
      .catch(() => {
        setPricingsError('Failed to load existing membership pricing.');
      })
      .finally(() => {
        setPricingsLoading(false);
      });
  }, []);

  const currentActivePricing = useMemo(() => {
    if (!selectedMembershipType) {
      return undefined;
    }

    return pricings.find(
      (pricing) => pricing.membershipType === selectedMembershipType && pricing.active
    );
  }, [pricings, selectedMembershipType]);

  const exactMatchPricing = useMemo(() => {
    if (!selectedMembershipType || price === undefined || price === null) {
      return undefined;
    }

    return pricings.find(
      (pricing) => pricing.membershipType === selectedMembershipType && Number(pricing.price) === Number(price)
    );
  }, [pricings, selectedMembershipType, price]);

  const handleMembershipTypeChange = () => {
    form.setFieldsValue({ price: undefined });
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
        membershipType: pendingValues.membershipType,
        price: pendingValues.price,
      };

      await createMembershipPricing(payload);

      form.resetFields();
      setFormTouched(false);

      message.success(
        exactMatchPricing
          ? 'Existing membership pricing reactivated successfully'
          : 'Membership type created successfully'
      );

      setConfirmOpen(false);
      setPendingValues(null);

      navigate('/memberships-type');
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to create membership type');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (formTouched) {
      Modal.confirm({
        title: 'Discard changes?',
        content: 'You have unsaved membership type details. Are you sure you want to leave?',
        okText: 'Discard',
        cancelText: 'Stay',
        onOk: () => navigate('/memberships-type'),
      });

      return;
    }

    navigate('/memberships-type');
  };

  useBeforeUnload(formTouched && !submitting);

  if (!isAdmin) {
    return (
      <div>
        <Alert
          type="warning"
          message="Access Denied"
          description="You are not authorized to create or manage membership pricing."
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Button onClick={() => navigate('/memberships-type')}>
          Back to Membership Types
        </Button>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Breadcrumb
          items={[
            { title: 'Home', onClick: () => navigate('/dashboard') },
            { title: 'Membership Types', onClick: () => navigate('/memberships-type') },
            { title: 'Add Membership Type' },
          ]}
        />

        <Button
          onClick={handleCancel}
          disabled={submitting || confirmOpen || resetConfirmOpen}
        >
          ← Back to Membership Types
        </Button>
      </div>

      <div style={{ marginBottom: 24 }}>
        <Typography.Title level={2} style={{ marginBottom: 4 }}>
          Create Membership Type
        </Typography.Title>

        <Typography.Text type="secondary">
          Configure a price for a membership type.
        </Typography.Text>
      </div>

      {pricingsError && (
        <Typography.Text type="danger" style={{ display: 'block', marginBottom: 16 }}>
          {pricingsError}
        </Typography.Text>
      )}

      <Spin spinning={submitting} tip="Creating membership type...">
        <Form form={form} layout="vertical" onFinish={handleSubmit} onValuesChange={() => setFormTouched(true)}>
          <Card title="Membership Type Information" size="small" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Membership Type"
              name="membershipType"
              rules={[{ required: true, message: 'Membership type is required' }]}
            >
              <Select
                placeholder="Select membership type"
                options={MEMBERSHIP_TYPE_OPTIONS}
                loading={pricingsLoading}
                onChange={handleMembershipTypeChange}
              />
            </Form.Item>

            {selectedMembershipType && currentActivePricing && (
              <Alert
                type="info"
                showIcon
                message={`Current active price for ${formatMembershipType(selectedMembershipType)} is ${formatCurrency(currentActivePricing.price)}. Saving a new price below will deactivate it.`}
                style={{ marginBottom: 16 }}
              />
            )}

            <Form.Item
              label="Price"
              name="price"
              tooltip={`Minimum price is ${formatCurrency(MINIMUM_PRICE)}.`}
              rules={[
                { required: true, message: 'Price is required' },
                {
                  validator: (_, value) => {
                    if (value === undefined || value === null) {
                      return Promise.resolve();
                    }

                    if (value < MINIMUM_PRICE) {
                      return Promise.reject(new Error(`Price must be at least ${MINIMUM_PRICE}`));
                    }

                    if (exactMatchPricing?.active) {
                      return Promise.reject(
                        new Error('Pricing with the same membership type and price is already active')
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
                disabled={!selectedMembershipType}
                placeholder={selectedMembershipType ? `Enter price (minimum ${MINIMUM_PRICE})` : 'Select a membership type first'}
              />
            </Form.Item>

            {exactMatchPricing && !exactMatchPricing.active && (
              <Alert
                type="warning"
                showIcon
                message={`An inactive price of ${formatCurrency(exactMatchPricing.price)} already exists for ${formatMembershipType(selectedMembershipType)}. Saving will reactivate that record instead of creating a new one.`}
              />
            )}
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
                Create Membership Type
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Spin>

      <Modal
        title="Create Membership Type"
        open={confirmOpen}
        onCancel={() => {
          if (!submitting) {
            setConfirmOpen(false);
            setPendingValues(null);
          }
        }}
        onOk={handleConfirmSubmit}
        okText={exactMatchPricing && !exactMatchPricing.active ? 'Reactivate Price' : 'Create Membership Type'}
        confirmLoading={submitting}
        cancelText="Cancel"
      >
        <p>
          Are you sure you want to{' '}
          {exactMatchPricing && !exactMatchPricing.active ? 'reactivate this price' : 'save this membership type'}?
        </p>

        {pendingValues && (
          <div style={{ marginTop: 16 }}>
            <p>
              <strong>Membership Type:</strong>{' '}
              {MEMBERSHIP_TYPE_OPTIONS.find((option) => option.value === pendingValues.membershipType)?.label}
            </p>
            <p><strong>Price:</strong> {formatCurrency(pendingValues.price)}</p>
            {currentActivePricing && (
              <p>
                <strong>Note:</strong> the currently active price of {formatCurrency(currentActivePricing.price)} for this
                membership type will be deactivated.
              </p>
            )}
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
          setResetConfirmOpen(false);
        }}
        okText="Reset"
        cancelText="Cancel"
      >
        <p>Are you sure you want to reset the form?</p>
        <p>All entered membership type details will be cleared.</p>
      </Modal>
    </div>
  );
}

export default CreateMembershipTypePage;
