# sky-take-out 开发笔记

## 接口开发流程（以员工分页查询为例）

### 1. 分析需求和接口定义
- 确认请求方式（GET / POST）
- GET 请求参数以问号形式拼接在 URL 后（`?page=1&pageSize=10&name=张`）
- POST 请求参数放在请求体 JSON 中

### 2. Controller
- 添加接口方法，GET 用 `@GetMapping`，参数不加 `@RequestBody`
- 返回值统一包装为 `Result<PageResult>`

```java
@GetMapping("/page")
@ApiOperation("员工分页查询")
public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
    PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
    return Result.success(pageResult);
}
```

### 3. Service 接口
- 在 `EmployeeService` 中声明方法

```java
PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
```

### 4. ServiceImpl
- 用 PageHelper 设置分页参数，再调用 Mapper
- PageHelper 会自动在 SQL 后拼接 `LIMIT offset, pageSize`

```java
PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
return new PageResult(page.getTotal(), page.getResult());
```

### 5. Mapper 接口
- 返回值用 PageHelper 的 `Page<T>`

```java
Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
```

### 6. Mapper XML
- 有动态条件时用 XML 而不是注解
- `<where>` 标签自动处理 WHERE 关键字
- `<if>` 判断参数是否传入
- 模糊查询用 `concat` + `#{}` 防 SQL 注入

```xml
<select id="pageQuery" resultType="com.sky.entity.Employee">
    select * from employee
    <where>
        <if test="name != null and name != ''">
            and name like concat('%', #{name}, '%')
        </if>
    </where>
    order by create_time desc
</select>
```

---

## 为什么分页查询用 XML 而登录用注解

| 场景 | 方式 | 原因 |
|---|---|---|
| 固定 SQL（登录、新增） | `@Select` `@Insert` 注解 | SQL 简单，一行搞定 |
| 动态 SQL（分页查询） | XML | 需要 `<if>` `<where>` 等标签 |

---

## 浏览器存储

- **Cookie**：存在客户端，每次请求自动携带，可设过期时间，4KB 限制
- **localStorage**：存在客户端，需手动读取，永久保存，5MB 限制
- **sessionStorage**：关标签页清空
- **选择原则**：需要服务器自动收到用 Cookie，前端自己用 localStorage

---

## Spring Boot 后端架构

- **knife4j（Swagger增强版）**：后端自带，端口同后端 8080，用于接口调试
- **拦截器 Interceptor**：请求到达 Controller 前的关卡，本项目用于验证 JWT token；登录接口通过 `excludePathPatterns` 排除在外
- **ThreadLocal（BaseContext）**：同一请求内跨 class 共享数据，本项目用于存当前登录用户 id，请求结束后 `remove()` 防止线程复用时数据污染

---

## JWT 认证机制

- **流程**：登录 → 后端生成 token → 前端保存 → 每次请求携带 → 后端验证
- **无状态**：服务器不记忆用户，每次靠 token 识别身份，符合 RESTful 设计；Tomcat 每个请求独立线程，天然无状态
- **token 过期策略**：
  - 直接过期：过期返回 401，前端跳登录页，简单但体验差
  - 无感刷新：access token（短期）+ refresh token（长期），自动换新 token，用户无感知
- **相关代码**：`JwtUtil`、`JwtProperties`、`JwtTokenAdminInterceptor`、`WebMvcConfiguration`

---

## Lombok

- `@Data`：自动生成 getter/setter/toString/equals/hashCode
- `@Slf4j`：自动注入 `log` 对象
- `@Builder`：建造者模式（链式创建对象）
- `@NoArgsConstructor` / `@AllArgsConstructor`：无参/全参构造方法

---

## 统一响应格式

- **`Result<T>`**：所有接口的统一外壳，包含 `code`（1成功/0失败）、`msg`、`data`
- **`PageResult`**：分页专用，包含 `total`（总记录数）和 `records`（当前页数据），作为 `Result` 的 `data` 字段

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 100,
    "records": [...]
  }
}
```
