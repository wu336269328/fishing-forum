import assert from 'node:assert/strict'
import { createRequestErrorNotifier } from '../src/api/errorMessage.js'

let now = 0
const messages = []
const notifier = createRequestErrorNotifier((message) => messages.push(message), {
  cooldownMs: 1000,
  now: () => now,
})

notifier('网络连接失败')
notifier('网络连接失败')
assert.deepEqual(messages, ['网络连接失败'])

notifier('没有权限执行此操作')
assert.deepEqual(messages, ['网络连接失败', '没有权限执行此操作'])

now = 1001
notifier('网络连接失败')
assert.deepEqual(messages, ['网络连接失败', '没有权限执行此操作', '网络连接失败'])
