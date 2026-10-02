import { useState } from 'react';
import {
  Alert,
  Breadcrumb,
  Button,
  Col,
  Empty,
  Form,
  Row,
  Spin,
  Typography,
} from 'antd';
import { useNavigate } from 'react-router-dom';

import { recommendExercises } from '../api/exerciseRecommendationApi';
import ExerciseDetailsModal from '../components/ExerciseDetailsModal';
import RecommendationCard from '../components/RecommendationCard';
import RecommendationSearchForm from '../components/RecommendationSearchForm';
import { RECOMMENDATION_LIMIT } from '../constants';
import '../components/ExerciseRecommendations.css';

const { Paragraph, Title } = Typography;

function ExerciseRecommendationsPage() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [recommendations, setRecommendations] = useState(null);
  const [selectedExercise, setSelectedExercise] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const findExercises = async (values) => {
    if (loading) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await recommendExercises({
        query: values.query.trim(),
        bodyPart: values.bodyPart || null,
        difficulty: values.difficulty || null,
        equipment: values.equipment ?? [],
        goal: values.goal || null,
        limit: RECOMMENDATION_LIMIT,
      });

      setRecommendations(response.data ?? []);
    } catch {
      setError('Unable to load exercise recommendations. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const clearSearch = () => {
    form.resetFields();
    setRecommendations(null);
    setSelectedExercise(null);
    setError(null);
  };

  return (
    <div className="exercise-recommendations-page">
      <Breadcrumb
        items={[
          { title: 'Home', onClick: () => navigate('/dashboard') },
          { title: 'Exercise Recommendations' },
        ]}
      />

      <header className="exercise-recommendations-header">
        <Title level={1}>Exercise Recommendations</Title>
        <Paragraph>Find exercises based on what you want to train.</Paragraph>
      </header>

      <RecommendationSearchForm
        form={form}
        loading={loading}
        onClear={clearSearch}
        onSubmit={findExercises}
      />

      <section className="exercise-results" aria-live="polite" aria-busy={loading}>
        <div className="exercise-results-heading">
          <Title level={2}>Recommended Exercises</Title>
          {recommendations?.length > 0 && (
            <span>{recommendations.length} matches</span>
          )}
        </div>

        {error && (
          <Alert
            type="error"
            showIcon
            message={error}
            action={<Button size="small" onClick={() => form.submit()}>Retry</Button>}
          />
        )}

        <Spin spinning={loading} tip="Finding exercises that match your request...">
          <div className="exercise-results-content">
            {recommendations === null ? (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="Describe what you are looking for to receive exercise recommendations."
              />
            ) : recommendations.length === 0 && !error ? (
              <Empty description="No matching exercises found. Try changing your description or removing some filters." />
            ) : (
              <Row gutter={[16, 16]}>
                {recommendations.map((recommendation) => (
                  <Col xs={24} xl={12} key={recommendation.exercise.id}>
                    <RecommendationCard
                      recommendation={recommendation}
                      onView={setSelectedExercise}
                    />
                  </Col>
                ))}
              </Row>
            )}
          </div>
        </Spin>
      </section>

      <ExerciseDetailsModal
        exercise={selectedExercise}
        onClose={() => setSelectedExercise(null)}
      />
    </div>
  );
}

export default ExerciseRecommendationsPage;
