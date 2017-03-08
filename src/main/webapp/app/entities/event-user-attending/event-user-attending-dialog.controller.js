(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventUserAttendingDialogController', EventUserAttendingDialogController);

    EventUserAttendingDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EventUserAttending', 'Event', 'User'];

    function EventUserAttendingDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EventUserAttending, Event, User) {
        var vm = this;

        vm.eventUserAttending = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.events = Event.query();
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.eventUserAttending.id !== null) {
                EventUserAttending.update(vm.eventUserAttending, onSaveSuccess, onSaveError);
            } else {
                EventUserAttending.save(vm.eventUserAttending, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventUserAttendingUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.createdDate = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
