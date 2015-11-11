
LOCAL_PATH := $(call my-dir)
HEBCALDIR := hebcal

include $(CLEAR_VARS)

LOCAL_MODULE    := hebcal
LOCAL_SRC_FILES := hebcal_ndk.c $(HEBCALDIR)/hebcal_android.c $(HEBCALDIR)/start.c $(HEBCALDIR)/dafyomi.c $(HEBCALDIR)/hebcal.c $(HEBCALDIR)/greg.c $(HEBCALDIR)/error.c $(HEBCALDIR)/danlib.c $(HEBCALDIR)/mygetopt.c $(HEBCALDIR)/rise.c $(HEBCALDIR)/common.c $(HEBCALDIR)/holidays.c $(HEBCALDIR)/sedra.c $(HEBCALDIR)/gnu.c

include $(BUILD_SHARED_LIBRARY)
