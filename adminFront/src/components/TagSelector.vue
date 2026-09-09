<template>
  <div class="tag-selector">
    <!-- 按类别分组渲染 -->
    <div v-for="group in groups" :key="group.code" class="tag-group">
      <p class="tag-category">{{ group.category }}</p>
      <div class="tag-grid">
        <button
          v-for="name in group.tags"
          :key="name"
          type="button"
          class="tag-chip"
          :class="{ selected: selectedTags.has(`${group.code}:${name}`) }"
          @click="toggleTag(group.code, name)"
        >
          {{ name }}
        </button>
      </div>
    </div>

    <div class="tag-footer">
      <span class="tag-count">已选 {{ selectedTags.size }} 个标签</span>
      <button v-if="selectedTags.size > 0" type="button" class="tag-clear" @click="clearTags">
        清空全部
      </button>
    </div>

    <div v-if="selectedTags.size > 0" class="tag-selected-list">
      <span v-for="key in selectedTags" :key="key" class="tag-selected-chip">
        {{ stripPrefix(key) }}
        <span class="tag-remove" @click="removeTag(key)">×</span>
      </span>
    </div>
    <div v-else class="tag-empty">尚未选择标签，请从上方点击选择</div>
    <div class="tag-hint">选择最能代表歌曲风格的标签，可多选</div>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { buildGroups, CODE_CATEGORY_MAP, PRESET_TAGS, resolveTagCode } from '@/constants/tags'
import { getEnabledTags } from '@/api/tag'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['update:modelValue'])

// 选中态：元素形如 "genre:流行"、"mood:欢快"，与存储值一一对应
const selectedTags = reactive(new Set())

// 标签分组：初始为预设常量（兜底），挂载后从服务端拉取启用标签替换
const groups = ref(PRESET_TAGS)

// module 级请求去重：同页多个选择器并发挂载时只发一次请求
let fetchPromise = null

/**
 * 将外部传入的 tags 字符串解析为选中集合。
 * - 带前缀的 "code:name"：命中当前分组则加入；
 *   类别码合法但已不在分组（如被禁用）也保留在已选列表，仅不能再新选
 * - 无前缀的 "name"：按名称唯一匹配当前分组后加入
 * - 孤儿标签（无前缀且不在字典内）：不兜底，等待迁移处理
 */
function parseTags(value) {
  const set = new Set()
  if (!value) return set
  const gs = groups.value
  value.split(',').forEach((token) => {
    const t = token.trim()
    if (!t) return
    const idx = t.indexOf(':')
    if (idx > 0) {
      const code = t.slice(0, idx)
      const name = t.slice(idx + 1)
      if (gs.some((g) => g.code === code && g.tags.includes(name))) {
        set.add(`${code}:${name}`)
      } else if (CODE_CATEGORY_MAP[code]) {
        set.add(`${code}:${name}`)
      }
    } else {
      const code = resolveTagCode(t, gs)
      if (code) set.add(`${code}:${t}`)
    }
  })
  return set
}

// 将选中集合同步回 props.modelValue 格式（回显解析时使用）
function resync() {
  selectedTags.clear()
  parseTags(props.modelValue).forEach((k) => selectedTags.add(k))
}

// 外部值变化时同步选中集合
watch(
  () => props.modelValue,
  () => resync(),
  { immediate: true }
)

// 动态分组就绪后重跑解析，保证已选 token 与新分组对齐
watch(groups, () => resync())

function syncToModel() {
  emit('update:modelValue', Array.from(selectedTags).join(','))
}

function toggleTag(code, name) {
  const key = `${code}:${name}`
  if (selectedTags.has(key)) {
    selectedTags.delete(key)
  } else {
    selectedTags.add(key)
  }
  syncToModel()
}

function removeTag(key) {
  selectedTags.delete(key)
  syncToModel()
}

function clearTags() {
  selectedTags.clear()
  syncToModel()
}

// 展示时剥离 "code:" 前缀，仅显示名称
function stripPrefix(key) {
  return key.slice(key.indexOf(':') + 1)
}

onMounted(() => {
  if (!fetchPromise) {
    fetchPromise = getEnabledTags()
      .then((res) => {
        const gs = buildGroups(res.data)
        if (gs.length > 0) {
          groups.value = gs
        }
      })
      .catch(() => {
        // 拉取失败时保留预设常量兜底，选择器仍可用
      })
      .finally(() => {
        fetchPromise = null
      })
  }
})
</script>

<style scoped lang="scss">
.tag-selector {
  width: 100%;
}

.tag-group {
  margin-bottom: 14px;
}

.tag-category {
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--wf-mute, #898989);
  margin: 0 0 8px;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  letter-spacing: -0.1px;
  border-radius: var(--rounded-sm, 4px);
  border: 1px solid var(--wf-hairline, #d8d8d8);
  background: var(--wf-canvas, #fff);
  color: var(--wf-body-mid, #5a5a5a);
  cursor: pointer;
  user-select: none;
  transition: all 0.15s ease;
  white-space: nowrap;
  outline: none;

  &:hover {
    border-color: var(--brand-accent, #4353ff);
    color: var(--brand-accent, #4353ff);
    background: rgba(67, 83, 255, 0.04);
  }

  &:active {
    transform: scale(0.96);
  }

  &.selected {
    background: var(--brand-accent, #4353ff);
    border-color: var(--brand-accent, #4353ff);
    color: #ffffff;

    &::after {
      content: "✕";
      font-size: 10px;
      margin-left: 4px;
      opacity: 0.7;
      font-weight: 600;
    }

    &:hover {
      background: var(--wf-accent-blue-info, #4353ff);
      border-color: var(--wf-accent-blue-info, #4353ff);
      color: #ffffff;

      &::after {
        opacity: 1;
      }
    }
  }
}

.tag-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 4px;
  margin-top: 12px;
  border-top: 1px solid var(--wf-hairline, #d8d8d8);
}

.tag-count {
  font-size: 12px;
  font-weight: 500;
  color: var(--wf-mute, #898989);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tag-clear {
  font-size: 12px;
  font-weight: 500;
  color: var(--wf-mute, #898989);
  cursor: pointer;
  background: none;
  border: none;
  font-family: inherit;
  padding: 2px 6px;
  border-radius: var(--rounded-sm, 4px);
  transition: all 0.15s;

  &:hover {
    color: var(--wf-accent-red, #ee1d36);
    background: rgba(238, 29, 54, 0.06);
  }
}

.tag-selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tag-selected-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 500;
  font-family: inherit;
  border-radius: var(--rounded-sm, 4px);
  background: rgba(67, 83, 255, 0.06);
  border: 1px solid rgba(67, 83, 255, 0.18);
  color: var(--brand-accent, #4353ff);
  transition: all 0.15s;
  animation: tagIn 150ms ease;

  .tag-remove {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    border-radius: 2px;
    font-size: 10px;
    font-weight: 600;
    cursor: pointer;
    opacity: 0.5;
    transition: all 0.15s;

    &:hover {
      opacity: 1;
      background: rgba(67, 83, 255, 0.15);
    }
  }
}

.tag-empty {
  font-size: 13px;
  font-weight: 400;
  color: var(--wf-mute-soft, #b0b0b0);
  margin-top: 8px;
}

.tag-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--wf-mute, #898989);
  margin-top: 4px;
}

@keyframes tagIn {
  from { opacity: 0; transform: scale(0.9); }
  to   { opacity: 1; transform: scale(1); }
}
</style>