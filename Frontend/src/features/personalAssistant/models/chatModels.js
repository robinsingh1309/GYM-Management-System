/**
 * @typedef {Object} ChatRequest
 * @property {string} question
 */

/**
 * @typedef {Object} ChatSource
 * @property {string} sourceFile
 * @property {number} pageNumber
 */

/**
 * @typedef {Object} ChatResponse
 * @property {string} answer
 * @property {ChatSource[]} sources
 */

/**
 * @typedef {Object} ChatMessage
 * @property {string} id
 * @property {'user' | 'assistant'} role
 * @property {string} content
 * @property {string} timestamp
 * @property {'sent' | 'error'} status
 * @property {ChatSource[]} sources
 * @property {string} [question]
 */

export {};
