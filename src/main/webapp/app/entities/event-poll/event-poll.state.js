(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('event-poll', {
            parent: 'entity',
            url: '/event-poll?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventPoll.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-poll/event-polls.html',
                    controller: 'EventPollController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventPoll');
                    $translatePartialLoader.addPart('eventPollStatus');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('event-poll-detail', {
            parent: 'entity',
            url: '/event-poll/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventPoll.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-poll/event-poll-detail.html',
                    controller: 'EventPollDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventPoll');
                    $translatePartialLoader.addPart('eventPollStatus');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EventPoll', function($stateParams, EventPoll) {
                    return EventPoll.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'event-poll',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('event-poll-detail.edit', {
            parent: 'event-poll-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-poll/event-poll-dialog.html',
                    controller: 'EventPollDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventPoll', function(EventPoll) {
                            return EventPoll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-poll.new', {
            parent: 'event-poll',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-poll/event-poll-dialog.html',
                    controller: 'EventPollDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                title: null,
                                description: null,
                                status: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('event-poll', null, { reload: 'event-poll' });
                }, function() {
                    $state.go('event-poll');
                });
            }]
        })
        .state('event-poll.edit', {
            parent: 'event-poll',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-poll/event-poll-dialog.html',
                    controller: 'EventPollDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventPoll', function(EventPoll) {
                            return EventPoll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-poll', null, { reload: 'event-poll' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-poll.delete', {
            parent: 'event-poll',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-poll/event-poll-delete-dialog.html',
                    controller: 'EventPollDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EventPoll', function(EventPoll) {
                            return EventPoll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-poll', null, { reload: 'event-poll' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
