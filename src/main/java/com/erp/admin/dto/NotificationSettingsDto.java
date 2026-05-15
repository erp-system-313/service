package com.erp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsDto {
    private boolean emailEnabled;
    private boolean leaveRequestSubmitted;
    private boolean leaveRequestApproved;
    private boolean leaveRequestRejected;
    private boolean attendanceReminder;
    private boolean applicantReceived;
}
