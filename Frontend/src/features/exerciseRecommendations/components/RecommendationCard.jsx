import { Button, Card, Tag, Typography } from 'antd';

const { Paragraph, Text, Title } = Typography;

function RecommendationCard({ recommendation, onView }) {
  const { exercise, similarityScore } = recommendation;
  const equipment = exercise.equipment ?? [];
  const secondaryMuscles = exercise.secondaryMuscles ?? [];
  const matchPercentage = Math.round(similarityScore * 100);

  return (
    <Card className="exercise-recommendation-card">
      <div className="exercise-card-heading">
        <Title level={3}>{exercise.name}</Title>
        <span className="exercise-match">{matchPercentage}% Match</span>
      </div>

      <div className="exercise-card-tags">
        {exercise.bodyPart && <Tag>{exercise.bodyPart}</Tag>}
        {exercise.difficulty && <Tag>{exercise.difficulty}</Tag>}
        {equipment.map((item) => <Tag key={item}>{item}</Tag>)}
      </div>

      {exercise.description && (
        <Paragraph className="exercise-description">{exercise.description}</Paragraph>
      )}

      <div className="exercise-muscles">
        {exercise.primaryMuscle && (
          <div>
            <Text type="secondary">Primary muscle</Text>
            <Text>{exercise.primaryMuscle}</Text>
          </div>
        )}

        {secondaryMuscles.length > 0 && (
          <div>
            <Text type="secondary">Also targets</Text>
            <Text>{secondaryMuscles.join(' · ')}</Text>
          </div>
        )}
      </div>

      <Button onClick={() => onView(exercise)}>View Exercise</Button>
    </Card>
  );
}

export default RecommendationCard;
