<template>
  <button
    class="back-button"
    :aria-label="ariaLabel"
    @click="handleBack"
  >
    <svg
      class="back-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <path d="M19 12H5M12 19l-7-7 7-7" />
    </svg>
    <span v-if="showText" class="back-text">{{ text }}</span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  fallback?: string  // 当没有历史记录时的备用路径
  text?: string
  showText?: boolean
  ariaLabel?: string
}

const props = withDefaults(defineProps<Props>(), {
  fallback: '/index',
  text: '返回',
  showText: true,
  ariaLabel: '返回上一页'
})

const router = useRouter()

const handleBack = () => {
  // 检查是否有历史记录可以返回
  if (window.history.length > 1) {
    router.back()
  } else {
    // 没有历史记录时，跳转到备用路径
    router.push(props.fallback)
  }
}
</script>

<style scoped lang="scss">
.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--color-bg-elevated, #fff);
  border: 1px solid var(--color-border, #e4e7ed);
  border-radius: 8px;
  color: var(--color-text-secondary, #606266);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

  &:hover {
    color: var(--color-primary, #5B6CF9);
    border-color: var(--color-primary, #5B6CF9);
    background: var(--color-primary-light, rgba(91, 108, 249, 0.08));
    transform: translateX(-2px);
  }

  &:active {
    transform: translateX(-4px);
  }

  &:focus-visible {
    outline: 2px solid var(--color-primary, #5B6CF9);
    outline-offset: 2px;
  }
}

.back-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.back-text {
  white-space: nowrap;
}
</style>
