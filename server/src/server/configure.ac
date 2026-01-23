#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.61)
AC_INIT(ecams_svr, 1.0.0)
AC_CONFIG_SRCDIR([svrsrc/ecams_svr.c])
AC_CONFIG_AUX_DIR([config])
AC_CONFIG_HEADER([config.h])

ENCAP_PKG([],[postinstall-encap])

PSG_MODULE([tarcom])

# Checks for programs.
AC_PROG_CC
AC_PROG_MAKE_SET

# Checks for libraries.

# Checks for header files.
AC_HEADER_DIRENT
AC_HEADER_STDC
AC_HEADER_MAJOR
AC_CHECK_HEADERS([arpa/inet.h fcntl.h limits.h netdb.h netinet/in.h stddef.h stdint.h stdlib.h string.h sys/param.h sys/socket.h sys/statvfs.h sys/time.h termio.h unistd.h utime.h])

PSG_REPLACE_TYPE([major_t], [unsigned int], [
  #include <sys/types.h>
  #ifdef MAJOR_IN_MKDEV
  # include <sys/mkdev.h>
  #else
  # ifdef MAJOR_IN_SYSMACROS
  #  include <sys/sysmacros.h>
  # endif
  #endif
])
PSG_REPLACE_TYPE([minor_t], [unsigned int], [
  #include <sys/types.h>
  #ifdef MAJOR_IN_MKDEV
  # include <sys/mkdev.h>
  #else
  # ifdef MAJOR_IN_SYSMACROS
  #  include <sys/sysmacros.h>
  # endif
  #endif
])
PSG_REPLACE_TYPE([dev_t], [unsigned long], [
  #include <sys/types.h>
  #ifdef MAJOR_IN_MKDEV
  # include <sys/mkdev.h>
  #else
  # ifdef MAJOR_IN_SYSMACROS
  #  include <sys/sysmacros.h>
  # endif
  #endif
])
PSG_REPLACE_TYPE([socklen_t], [unsigned long], [
  #include <sys/types.h>
  #include <sys/socket.h>
])
PSG_REPLACE_TYPE([uint64_t], [long long])
AC_TYPE_MODE_T
AC_TYPE_OFF_T
AC_TYPE_SIZE_T
AC_TYPE_UID_T
PSG_REPLACE_TYPE([nlink_t], [unsigned short])


# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_TYPE_OFF_T
AC_TYPE_UID_T
AC_TYPE_MODE_T
AC_TYPE_SIZE_T
AC_CHECK_MEMBERS([struct stat.st_rdev])
AC_HEADER_TIME
AC_STRUCT_TM
AC_TYPE_UINT32_T

# Checks for library functions.
AC_CHECK_LIB(nsl,main)
AC_CHECK_LIB(socket,main)
AC_CHECK_LIB(resolv,main)
AC_CHECK_LIB(xnet,main)
COMPAT_FUNC_BASENAME
COMPAT_FUNC_DIRNAME
COMPAT_FUNC_FNMATCH
AC_CHECK_FUNCS([lchown])
COMPAT_FUNC_MAKEDEV
COMPAT_FUNC_SNPRINTF
COMPAT_FUNC_STRDUP
AC_FUNC_STRFTIME
COMPAT_FUNC_STRLCPY
COMPAT_FUNC_STRMODE
COMPAT_FUNC_STRSEP

AC_CONFIG_FILES([Makefile
		 tarsrc/Makefile
                 aessrc/Makefile
                 ftpsrc/Makefile
                 md5src/Makefile
                 svrsrc/Makefile])
AC_OUTPUT
