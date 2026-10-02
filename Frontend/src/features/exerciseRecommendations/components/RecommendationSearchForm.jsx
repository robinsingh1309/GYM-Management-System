import { Button, Card, Col, Form, Input, Row, Select, Typography } from 'antd';

import {
  BODY_PART_OPTIONS,
  DIFFICULTY_OPTIONS,
  EQUIPMENT_OPTIONS,
  GOAL_OPTIONS,
} from '../constants';

const { Title } = Typography;
const selectOptions = (values) => values.map((value) => ({ label: value, value }));

function RecommendationSearchForm({ form, loading, onClear, onSubmit }) {
  return (
    <Card className="exercise-search-card">
      <Form form={form} layout="vertical" onFinish={onSubmit}>
        <Form.Item
          label="What kind of exercise are you looking for?"
          name="query"
          rules={[
            { required: true, message: 'Describe the exercise you are looking for.' },
            { whitespace: true, message: 'Describe the exercise you are looking for.' },
          ]}
        >
          <Input.TextArea
            className="exercise-query-input"
            autoSize={{ minRows: 2, maxRows: 4 }}
            placeholder="e.g. I want beginner chest exercises using dumbbells"
            disabled={loading}
          />
        </Form.Item>

        <Title level={2} className="exercise-filter-heading">Refine recommendations</Title>

        <Row gutter={[16, 0]}>
          <Col xs={24} md={12} xl={6}>
            <Form.Item label="Body part" name="bodyPart">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder="Any body part"
                options={selectOptions(BODY_PART_OPTIONS)}
                disabled={loading}
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Form.Item label="Difficulty" name="difficulty">
              <Select
                allowClear
                placeholder="Any difficulty"
                options={selectOptions(DIFFICULTY_OPTIONS)}
                disabled={loading}
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Form.Item label="Equipment" name="equipment">
              <Select
                mode="multiple"
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder="Any equipment"
                options={selectOptions(EQUIPMENT_OPTIONS)}
                disabled={loading}
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={12} xl={6}>
            <Form.Item label="Goal" name="goal">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder="Any goal"
                options={selectOptions(GOAL_OPTIONS)}
                disabled={loading}
              />
            </Form.Item>
          </Col>
        </Row>

        <div className="exercise-search-actions">
          <Button onClick={onClear} disabled={loading}>Clear</Button>
          <Button type="primary" htmlType="submit" loading={loading}>
            Find Exercises
          </Button>
        </div>
      </Form>
    </Card>
  );
}

export default RecommendationSearchForm;
