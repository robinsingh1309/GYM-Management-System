import { Descriptions, Modal, Tag, Typography } from 'antd';

const { Paragraph, Title } = Typography;

function ExerciseDetailsModal({ exercise, onClose }) {
  const hasValues = (values) => Array.isArray(values) && values.length > 0;

  return (
    <Modal
      title={exercise?.name || 'Exercise details'}
      open={Boolean(exercise)}
      onCancel={onClose}
      footer={null}
      width={720}
      destroyOnHidden
      rootClassName="exercise-details-modal"
    >
      {exercise && (
        <div className="exercise-details">
          <Descriptions bordered column={{ xs: 1, sm: 2 }} size="small">
            {exercise.bodyPart && (
              <Descriptions.Item label="Body part">{exercise.bodyPart}</Descriptions.Item>
            )}
            {exercise.difficulty && (
              <Descriptions.Item label="Difficulty">{exercise.difficulty}</Descriptions.Item>
            )}
            {exercise.primaryMuscle && (
              <Descriptions.Item label="Primary muscle">{exercise.primaryMuscle}</Descriptions.Item>
            )}
            {exercise.movementPattern && (
              <Descriptions.Item label="Movement pattern">{exercise.movementPattern}</Descriptions.Item>
            )}
            {exercise.exerciseType && (
              <Descriptions.Item label="Exercise type">{exercise.exerciseType}</Descriptions.Item>
            )}
            {hasValues(exercise.secondaryMuscles) && (
              <Descriptions.Item label="Also targets" span={2}>
                {exercise.secondaryMuscles.map((item) => <Tag key={item}>{item}</Tag>)}
              </Descriptions.Item>
            )}
            {hasValues(exercise.equipment) && (
              <Descriptions.Item label="Equipment" span={2}>
                {exercise.equipment.map((item) => <Tag key={item}>{item}</Tag>)}
              </Descriptions.Item>
            )}
            {hasValues(exercise.goalTags) && (
              <Descriptions.Item label="Goals" span={2}>
                {exercise.goalTags.map((item) => <Tag key={item}>{item}</Tag>)}
              </Descriptions.Item>
            )}
          </Descriptions>

          {exercise.description && (
            <section>
              <Title level={4}>About this exercise</Title>
              <Paragraph>{exercise.description}</Paragraph>
            </section>
          )}

          {hasValues(exercise.instructions) && (
            <section>
              <Title level={4}>How to perform</Title>
              <ol className="exercise-instructions">
                {exercise.instructions.map((instruction, index) => (
                  <li key={`${index}-${instruction}`}>{instruction}</li>
                ))}
              </ol>
            </section>
          )}
        </div>
      )}
    </Modal>
  );
}

export default ExerciseDetailsModal;
