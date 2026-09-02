package com.ackileo.telematics.data.remote

// JwtInterceptor has been intentionally removed.
//
// AuthInterceptor (registered in RetrofitModule.provideOkHttpClient) already:
//   • skips auth/login and auth/register bootstrap endpoints
//   • attaches "Authorization: Bearer <token>" to every other request
//   • clears auth state on HTTP 401
//
// Keeping a second interceptor that unconditionally attaches the token to ALL
// requests (including /auth/login) would be both redundant and incorrect.
