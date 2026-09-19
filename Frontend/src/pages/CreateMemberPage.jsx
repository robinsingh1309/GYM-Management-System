import { Breadcrumb, Button, Card, DatePicker, Form, Input, message, Modal, Select, Spin, Typography, } from 'antd';
import dayjs from 'dayjs';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createMember } from '../api/memberApi';
import { useBeforeUnload } from '../hooks/useBeforeUnload';

function CreateMemberPage() {
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const [submitting, setSubmitting] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingValues, setPendingValues] = useState(null);
  const [resetConfirmOpen, setResetConfirmOpen] = useState(false);
  const [formTouched, setFormTouched] = useState(false);

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
            ...pendingValues,
            name: pendingValues.name?.trim(),
            email: pendingValues.email?.trim().toLowerCase(),
            phoneNumber: pendingValues.phoneNumber?.trim(),
            address: pendingValues.address?.trim(),
            dateOfBirth: pendingValues.dateOfBirth?.format('YYYY-MM-DD'),
            joiningDate: pendingValues.joiningDate?.format('YYYY-MM-DD'),
        };

      await createMember(payload);

      form.resetFields();
      setFormTouched(false);

      message.success('Member created successfully');

      setConfirmOpen(false);
      setPendingValues(null);

      navigate('/members');
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to create member');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (formTouched) {
        Modal.confirm({
        title: 'Discard changes?',
        content: 'You have unsaved member details. Are you sure you want to leave?',
        okText: 'Discard',
        cancelText: 'Stay',
        onOk: () => navigate('/members'),
        });

        return;
    }

    navigate('/members');
  };

  useBeforeUnload(formTouched && !submitting);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Breadcrumb
          items={[
            { title: 'Home', onClick: () => navigate('/dashboard') },
            { title: 'Members', onClick: () => navigate('/members') },
            { title: 'Add Member' },
          ]}
        />

        <Button
          onClick={handleCancel}
          disabled={submitting || confirmOpen || resetConfirmOpen}
        >
          ← Back to Members
        </Button>
      </div>

      <div style={{ marginBottom: 24 }}>
        <Typography.Title level={2} style={{ marginBottom: 4 }}>
            Create Member
        </Typography.Title>

        <Typography.Text type="secondary">
            Add a new member to the gym.
        </Typography.Text>
      </div>

     <Spin spinning={submitting} tip="Creating member...">
      <Form form={form} layout="vertical" onFinish={handleSubmit} onValuesChange={() => setFormTouched(true)}>
        <Card title="Personal Information" size="small" style={{ marginBottom: 16 }}>
          <Form.Item
            label="Name"
            name="name"
            rules={[
              { required: true, message: 'Name is required' },
              { max: 100, message: 'Name cannot exceed 100 characters' },
            ]}
          >
            <Input placeholder="Enter member name" maxLength={100} showCount/>
          </Form.Item>

          <Form.Item
            label="Email"
            name="email"
            rules={[
              { required: true, message: 'Email is required' },
              { type: 'email', message: 'Please enter a valid email' },
              { max: 100, message: 'Email cannot exceed 100 characters' },
            ]}
          >
            <Input type="email" placeholder="Enter email address" maxLength={100} showCount/>
          </Form.Item>

          <Form.Item
            label="Phone Number"
            name="phoneNumber"
            rules={[
              { required: true, message: 'Phone number is required' },
              { pattern: /^[6-9]\d{9}$/, message: 'Phone number must be a valid 10 digit Indian phone number' },
            ]}
          >
            <Input
              placeholder="Enter phone number"
              maxLength={10}
              inputMode="numeric"
              onChange={(e) => {
                const digitsOnly = e.target.value.replace(/[^0-9]/g, '');
                form.setFieldsValue({ phoneNumber: digitsOnly });
              }}
            />
          </Form.Item>

          <Form.Item
            label="Date of Birth"
            name="dateOfBirth"
            dependencies={['joiningDate']}
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value) {
                    return Promise.resolve();
                  }

                  if (value.isAfter(dayjs(), 'day')) {
                    return Promise.reject(new Error('Date of birth cannot be in the future'));
                  }

                  const joiningDate = getFieldValue('joiningDate');

                  if (joiningDate && value.isAfter(joiningDate, 'day')) {
                    return Promise.reject(new Error('Date of birth cannot be after joining date'));
                  }

                  if (joiningDate && joiningDate.diff(value, 'year') < 6) {
                    return Promise.reject(new Error('Member must be at least 6 years old on joining date'));
                  }

                  return Promise.resolve();
                },
              }),
            ]}
          >
            <DatePicker
              style={{ width: '100%' }}
              disabledDate={(current) => current && current.isAfter(dayjs(), 'day')}
            />
          </Form.Item>

          <Form.Item label="Gender" name="gender">
            <Select
              placeholder="Select gender"
              allowClear
              options={[
                { value: 'MALE', label: 'Male' },
                { value: 'FEMALE', label: 'Female' },
                { value: 'OTHER', label: 'Other' },
              ]}
            />
          </Form.Item>
        </Card>

        <Card title="Contact Information" size="small" style={{ marginBottom: 16 }}>
          <Form.Item
            label="Address"
            name="address"
            rules={[{ max: 255, message: 'Address cannot exceed 255 characters' }]}
          >
            <Input.TextArea rows={3} placeholder="Enter member address" maxLength={255} showCount/>
          </Form.Item>
        </Card>

        <Card title="Membership Information" size="small" style={{ marginBottom: 16 }}>
          <Form.Item
            label="Joining Date"
            name="joiningDate"
            dependencies={['dateOfBirth']}
            rules={[
              { required: true, message: 'Joining date is required' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const dateOfBirth = getFieldValue('dateOfBirth');

                  if (!value || !dateOfBirth) {
                    return Promise.resolve();
                  }

                  if (dateOfBirth.isAfter(value, 'day')) {
                    return Promise.reject(new Error('Joining date cannot be before date of birth'));
                  }

                  if (value.diff(dateOfBirth, 'year') < 6) {
                    return Promise.reject(new Error('Member must be at least 6 years old on joining date'));
                  }

                  return Promise.resolve();
                },
              }),
            ]}
          >
            <DatePicker
              style={{ width: '100%' }}
              disabledDate={(current) => current && current.isBefore(dayjs(), 'day')}
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
              Create Member
            </Button>
          </div>
        </Form.Item>
      </Form>
      </Spin>

      <Modal
        title="Create Member"
        open={confirmOpen}
        onCancel={() => {
          if (!submitting) {
            setConfirmOpen(false);
            setPendingValues(null);
          }
        }}
        onOk={handleConfirmSubmit}
        okText="Create Member"
        confirmLoading={submitting}
        cancelText="Cancel"
      >
        <p>Are you sure you want to create this member?</p>

        {pendingValues && (
          <div style={{ marginTop: 16 }}>
            <p><strong>Name:</strong> {pendingValues.name}</p>
            <p><strong>Email:</strong> {pendingValues.email}</p>
            <p><strong>Phone:</strong> {pendingValues.phoneNumber}</p>
            <p>
              <strong>Joining Date:</strong>{' '}
              {pendingValues.joiningDate?.format('YYYY-MM-DD')}
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
          setResetConfirmOpen(false);
        }}
        okText="Reset"
        cancelText="Cancel"
      >
        <p>Are you sure you want to reset the form?</p>
        <p>All entered member details will be cleared.</p>
      </Modal>
    </div>
  );
}

export default CreateMemberPage;
