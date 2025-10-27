// src/main/resources/static/admin/assets/static/js/components/dark.js
const THEME_KEY = "theme"

function toggleDarkTheme() {
  setTheme(
      document.documentElement.getAttribute("data-bs-theme") === 'dark'
          ? "light"
          : "dark"
  )
}

function setTheme(theme, persist = false) {

  document.documentElement.setAttribute('data-bs-theme', theme)

  if (persist) {
    localStorage.setItem(THEME_KEY, theme)
  }
}

function initTheme() {
  const storedTheme = localStorage.getItem(THEME_KEY)
  if (storedTheme) {
    return setTheme(storedTheme, true) // Đặt persist = true
  }

  if (!window.matchMedia) {
    return
  }

  const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)")
  mediaQuery.addEventListener("change", (e) =>
      setTheme(e.matches ? "dark" : "light", true)
  )
  return setTheme(mediaQuery.matches ? "dark" : "light", true)
}

window.addEventListener('DOMContentLoaded', () => {
  const toggler = document.getElementById("toggle-dark")

  initTheme()

  const theme = localStorage.getItem(THEME_KEY) // Đọc lại sau khi init

  if(toggler) {
    toggler.checked = theme === "dark"

    toggler.addEventListener("input", (e) => {
      setTheme(e.target.checked ? "dark" : "light", true)
    })
  }
});