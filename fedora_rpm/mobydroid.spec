%define _binaries_in_noarch_packages_terminate_build   0
Name:           mobydroid
Version:        0.4
Release:        1%{?dist}
Summary:        GUI for Android SDK/ADB
BuildArch:	noarch
License:        ASL 2.0
URL:            https://github.com/ibilux/MobyDroid
Source0:        https://github.com/ibilux/MobyDroid/releases/download/v%{version}/MobyDroid_v%{version}.zip
Source1:	https://raw.githubusercontent.com/ibilux/MobyDroid/master/fedora_rpm/mobydroid.desktop
Source2:	https://raw.githubusercontent.com/ibilux/MobyDroid/master/fedora_rpm/mobydroid.png
Source3:	https://raw.githubusercontent.com/ibilux/MobyDroid/master/fedora_rpm/mobydroid
Requires:	android-tools
Requires:	jre >= 1.8.0

%description
GUI for Android SDK/ADB, Android phone manager.

%prep
%autosetup -c -n %{name}-%{version}


%install
rm -rf $RPM_BUILD_ROOT
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_datadir}/%{name}/bin
mkdir -p %{buildroot}%{_datadir}/applications
mkdir -p %{buildroot}%{_datadir}/pixmaps

install -Dm755 ./%{name}.jar %{buildroot}%{_datadir}/%{name}/%{name}.jar
install -Dm644 ./bin/busybox-arm %{buildroot}%{_datadir}/%{name}/bin/busybox-arm
install -Dm644 ./bin/busybox-x86 %{buildroot}%{_datadir}/%{name}/bin/busybox-x86
install -Dm644 ./bin/aapt-arm %{buildroot}%{_datadir}/%{name}/bin/aapt-arm
install -Dm644 ./bin/aapt-x86 %{buildroot}%{_datadir}/%{name}/bin/aapt-x86

install -Dm644 %{SOURCE1} %{buildroot}%{_datadir}/applications/mobydroid.desktop
install -Dm644 %{SOURCE2} %{buildroot}%{_datadir}/pixmaps/mobydroid.png
install -Dm755 %{SOURCE3} %{buildroot}%{_bindir}/mobydroid

%files
%{_datadir}/mobydroid/mobydroid.jar
%{_datadir}/mobydroid/bin/busybox-arm
%{_datadir}/mobydroid/bin/busybox-x86
%{_datadir}/mobydroid/bin/aapt-arm
%{_datadir}/mobydroid/bin/aapt-x86
%{_datadir}/applications/mobydroid.desktop
%{_datadir}/pixmaps/mobydroid.png
%{_bindir}/mobydroid


%changelog
* Thu Apr 29 2021 bilux <i.bilux@gmail.com> 0.4-1
* Wed May  6 2020 bilux <i.bilux@gmail.com> 0.3-1
* Thu Dec  5 2018 yucef sourani <youssef.m.sourani@gmail.com> 0.2-1
- Initial For Fedora
- Update to version 0.4

