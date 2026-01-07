// 为浏览器环境提供全局变量垫片
// 这将解决 sockjs-client 中 "global is not defined" 的错误

// 定义全局变量，如果尚未定义
if (typeof global === "undefined") {
  (window as any).global = globalThis;
}

// 为 Buffer 提供全局访问
if (typeof Buffer === "undefined") {
  (window as any).Buffer = (await import("buffer")).Buffer;
}

// 为 process 提供全局访问
if (typeof process === "undefined") {
  (window as any).process = (await import("process")).default;
}