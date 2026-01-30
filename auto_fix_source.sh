#!/bin/bash
# auto_fix_source.sh - 소스 코드 부분 자동 패치 스크립트
#
# 목적: build_all.sh에서 분리된 소스 코드 자동 패치 기능
#
# 해결하는 문제:
#   - Deprecated 함수 (bzero/bcopy → memset/memcpy)
#   - Alpine Linux BSD 타입 (u_char, u_short, u_int, u_long)
#   - 네트워크 헤더 (lanapi.c만)
#
# 해결하지 못하는 문제 (수동 작업 필요):
#   - 타입 정의 누락 (socklen_t, uint64_t, dev_t 등)
#   - basename/th_read 중복 정의
#   - Solaris statvfs OS 분기 처리
#   - Makefile.in 링커 순서
#   - 형변환/Format String 경고
#
# 사용법: ./auto_fix_source.sh

# 에러 발생시 계속 진행 (부분 패치이므로)
set +e

#===================================================================
# 로깅 함수
#===================================================================
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✓ $1"
}

log_warn() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠ $1"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ $1"
}

#===================================================================
# 메인 패치 함수
#===================================================================
main() {
    log "=========================================="
    log "ECAMS Agent 소스 코드 자동 패치"
    log "=========================================="
    log ""
    log "자동 해결: Deprecated 함수, BSD 타입, 네트워크 헤더"
    log "수동 필요: 타입 정의, 중복 정의, Solaris statvfs, 링커 순서 등"
    log ""

    #=================================================================
    # 1. Deprecated 함수 치환 (bzero/bcopy → memset/memcpy)
    #=================================================================
    log "=========================================="
    log "Step 1: Deprecated 함수 치환 (bzero/bcopy)"
    log "=========================================="
    
    REPLACED_COUNT=0
    TOTAL_FILES=0
    
    # 모든 .c, .h 파일 검색
    while IFS= read -r file; do
        TOTAL_FILES=$((TOTAL_FILES + 1))
        
        if grep -q "bzero\|bcopy" "$file" 2>/dev/null; then
            # 백업 생성
            cp "$file" "$file.autofix.bak"
            
            # bzero → memset 치환
            sed -i 's/bzero(\([^,]*\),\s*\([^)]*\))/memset(\1, 0, \2)/g' "$file"
            
            # bcopy → memcpy 치환 (인자 순서 주의!)
            sed -i 's/bcopy(\([^,]*\),\s*\([^,]*\),\s*\([^)]*\))/memcpy(\2, \1, \3)/g' "$file"
            
            log "  ✓ $file"
            REPLACED_COUNT=$((REPLACED_COUNT + 1))
        fi
    done < <(find . -type f \( -name "*.c" -o -name "*.h" \))
    
    log ""
    log_success "검사한 파일: $TOTAL_FILES 개"
    log_success "수정한 파일: $REPLACED_COUNT 개"
    log ""

    #=================================================================
    # 2. BSD 타입 정의 추가 (Alpine Linux / musl libc 대응)
    #=================================================================
    log "=========================================="
    log "Step 2: BSD 타입 정의 추가 (u_char 등)"
    log "=========================================="
    
    HEADER_FILE="inc/ecamsapi.h"
    
    if [ -f "$HEADER_FILE" ]; then
        if ! grep -q "typedef unsigned char u_char" "$HEADER_FILE"; then
            # 백업 생성
            cp "$HEADER_FILE" "$HEADER_FILE.autofix.bak"
            
            # 임시 파일에 타입 정의 추가
            cat > "${HEADER_FILE}.tmp" << 'EOF'
/* BSD compatibility types for Alpine Linux / musl libc */
#ifndef u_char
typedef unsigned char u_char;
typedef unsigned short u_short;
typedef unsigned int u_int;
typedef unsigned long u_long;
#endif

EOF
            # 원본 파일 내용 추가
            cat "$HEADER_FILE" >> "${HEADER_FILE}.tmp"
            
            # 원본 파일 교체
            mv "${HEADER_FILE}.tmp" "$HEADER_FILE"
            
            log_success "$HEADER_FILE - BSD 타입 추가됨"
        else
            log "  ○ $HEADER_FILE - BSD 타입 이미 존재"
        fi
    else
        log_warn "$HEADER_FILE 파일 없음 - SKIP"
    fi
    log ""

    #=================================================================
    # 3. 네트워크 헤더 추가 (lanapi.c만)
    #=================================================================
    log "=========================================="
    log "Step 3: 네트워크 헤더 추가 (lanapi.c)"
    log "=========================================="
    
    LANAPI_FILE="svrsrc/lanapi.c"
    
    if [ -f "$LANAPI_FILE" ]; then
        if ! grep -q "#include <sys/select.h>" "$LANAPI_FILE"; then
            # 백업 생성
            cp "$LANAPI_FILE" "$LANAPI_FILE.autofix.bak"
            
            # sys/socket.h 다음에 헤더 추가
            sed -i '/#include <sys\/socket.h>/a\
#include <sys/select.h>\
#include <arpa/inet.h>\
#include <strings.h>    /* bzero, bcopy */
' "$LANAPI_FILE"
            
            log_success "$LANAPI_FILE - 헤더 추가됨"
        else
            log "  ○ $LANAPI_FILE - 헤더 이미 존재"
        fi
    else
        log_warn "$LANAPI_FILE 파일 없음 - SKIP"
    fi
    log ""

    #=================================================================
    # 완료 메시지
    #=================================================================
    log "=========================================="
    log_success "자동 패치 완료!"
    log "=========================================="
    log ""
    log "백업 파일: *.autofix.bak"
    log ""
    log "⚠️  다음 작업은 수동으로 진행 필요:"
    log ""
    log "  [우선순위 높음]"
    log "  1. configure.ac 수정 (타입 정의, 함수 체크)"
    log "     - AC_CHECK_FUNCS([basename select socket gethostbyname])"
    log "     - AC_CHECK_HEADERS([sys/statvfs.h sys/vfs.h])"
    log "     - PSG_REPLACE_TYPE([socklen_t], [unsigned long], ...)"
    log ""
    log "  2. basename 조건부 컴파일 (tarcom/basename.c)"
    log "     - #ifndef HAVE_BASENAME로 전체 감싸기"
    log ""
    log "  3. Solaris statvfs 분기 (svrsrc/util.c)"
    log "     - #ifdef __sun 분기 처리"
    log ""
    log "  4. Makefile.in 링커 순서 조정"
    log "     - ecams_svr 타겟의 object 파일 순서 명시"
    log ""
    log "  [우선순위 중간]"
    log "  5. 형변환 경고 - intptr_t/uintptr_t 사용"
    log "  6. Format String 경고 - %zu, %lld 등 올바른 포맷"
    log ""
    log "자세한 내용은 06.build_all_미반영_문제목록.md 참고"
    log ""
}

# 스크립트 실행
main "$@"
